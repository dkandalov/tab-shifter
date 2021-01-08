package tabshifter

import com.intellij.openapi.diagnostic.Logger
import tabshifter.valueobjects.*

class TabShifter(private val ide: Ide) {
    /**
     * Potentially this mysterious component com.intellij.ui.switcher.SwitchManagerAppComponent
     * could be used for switching focus, but it's currently doesn't work very well and is not enabled.
     */
    fun moveFocus(direction: Direction) {
        val layout = ide.snapshotWindowLayout()?.calculateAndSetPositions() ?: return
        val currentWindow = layout.currentWindow() ?: return
        val targetWindow = direction.findTargetWindow(currentWindow, layout) ?: return
        ide.setFocusOn(targetWindow)
    }

    /**
     * Moves tab in the specified direction.
     *
     * This is way more complicated than it should have been. The main reasons are:
     * - closing/opening or opening/closing tab doesn't guarantee that focus will be in the moved tab,
     *   therefore, need to track target window to move focus into it
     * - EditorWindow object changes its identity after split/unsplit (i.e. points to another visual window)
     *   therefore, need to predict target window position and look up window by expected position
     */
    fun moveTab(direction: Direction) {
        val layout = ide.snapshotWindowLayout()?.calculateAndSetPositions() ?: return
        val currentWindow = layout.currentWindow() ?: return
        val targetWindow = direction.findTargetWindow(currentWindow, layout)

        val newPosition: Position
        if (targetWindow == null) {
            if (currentWindow.hasOneTab || !direction.canExpand) return
            val newLayout = insertSplit(direction.splitOrientation, currentWindow, layout)
            newLayout.calculateAndSetPositions()
            val sibling = findSiblingOf(currentWindow, newLayout) ?: return // should never happen
            newPosition = sibling.position
            ide.createSplitter(direction.splitOrientation)
        } else {
            val willBeUnsplit = currentWindow.hasOneTab
            if (willBeUnsplit) {
                val unsplitLayout = removeFrom(layout, currentWindow)
                unsplitLayout?.calculateAndSetPositions()
            }
            newPosition = targetWindow.position
            ide.openCurrentFileIn(targetWindow)
        }
        ide.closeCurrentFileIn(currentWindow) {
            val newWindowLayout = ide.snapshotWindowLayout()?.calculateAndSetPositions()
            // Do this because identity of the window object can change after closing the current file.
            val targetWindowLookedUpAgain = newWindowLayout.traverse().filterIsInstance<Window>().find { it.position == newPosition }
            if (targetWindowLookedUpAgain == null) {
                // Ideally, this should never happen, logging in case something goes wrong.
                logger.warn("No window for: $newPosition; windowLayout: $newWindowLayout")
            } else {
                ide.setFocusOn(targetWindowLookedUpAgain)
            }
        }
    }

    fun stretchSplitter(direction: Direction) {
        val layout = ide.snapshotWindowLayout()?.calculateAndSetPositions() ?: return
        val currentWindow = layout.currentWindow() ?: return
        var split = findParentSplitOf(currentWindow, layout)
        val orientationToSkip = if (direction == left || direction == right) Split.Orientation.horizontal else Split.Orientation.vertical
        while (split != null && split.orientation == orientationToSkip) {
            split = findParentSplitOf(split, layout)
        }
        if (split == null) return
        if (direction == right || direction == down) {
            ide.growSplitProportion(split)
        } else {
            ide.shrinkSplitProportion(split)
        }
    }

    fun toggleMaximizeRestoreSplitter() {
        val layout = ide.snapshotWindowLayout()?.calculateAndSetPositions() ?: return
        val currentWindow = layout.currentWindow() ?: return
        val split = findParentSplitOf(currentWindow, layout) ?: return

        val inFirst = split.first == currentWindow
        val maximized = ide.toggleMaximizeRestoreSplitter(split, inFirst)
        if (maximized) ide.hideToolWindows()
    }

    fun equalSizeSplitter() {
        val layout = ide.snapshotWindowLayout()?.calculateAndSetPositions() ?: return
        val split = findParentSplitOf(layout.currentWindow() ?: return, layout) ?: return
        ide.equalSizeSplitter(split)
    }

    companion object {
        private val logger = Logger.getInstance(TabShifter::class.java.name)

        private fun findParentSplitOf(layoutElement: LayoutElement, layout: LayoutElement): Split? =
            layout.traverse().filterIsInstance<Split>()
                .find { it.first == layoutElement || it.second == layoutElement }

        private fun LayoutElement.currentWindow() =
            traverse().filterIsInstance<Window>().find { it.isCurrent }

        private fun findSiblingOf(window: Window, element: LayoutElement): LayoutElement? =
            when (element) {
                is Split  -> when {
                    element.first == window  -> element.second
                    element.second == window -> element.first
                    else                     -> findSiblingOf(window, element.first) ?: findSiblingOf(window, element.second)
                }
                is Window -> null
                else      -> throw IllegalStateException()
            }

        private fun LayoutElement.calculateAndSetPositions(position: Position = Position(0, 0, size.width, size.height)): LayoutElement {
            if (this is Split) {
                val firstPosition: Position
                val secondPosition: Position
                if (orientation == Split.Orientation.vertical) {
                    firstPosition = position.withToX(position.toX - second.size.width)
                    secondPosition = position.withFromX(position.fromX + first.size.width)
                } else {
                    firstPosition = position.withToY(position.toY - second.size.height)
                    secondPosition = position.withFromY(position.fromY + first.size.height)
                }
                first.calculateAndSetPositions(firstPosition)
                second.calculateAndSetPositions(secondPosition)
            }
            this.position = position
            return this
        }

        private fun removeFrom(element: LayoutElement, window: Window): LayoutElement? =
            when (element) {
                is Split  -> {
                    val first = removeFrom(element.first, window)
                    val second = removeFrom(element.second, window)
                    if (first == null) second else if (second == null) first else Split(first, second, element.orientation)
                }
                is Window -> if (element == window) null else element
                else      -> throw IllegalStateException()
            }

        private fun insertSplit(orientation: Split.Orientation, window: Window, element: LayoutElement): LayoutElement =
            when (element) {
                is Split  -> Split(
                    insertSplit(orientation, window, element.first),
                    insertSplit(orientation, window, element.second),
                    element.orientation
                )
                is Window -> if (element == window) Split(window, Window(hasOneTab = true, isCurrent = false), orientation) else element
                else      -> throw IllegalStateException()
            }
    }
}