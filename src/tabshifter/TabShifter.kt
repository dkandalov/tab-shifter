package tabshifter

import com.intellij.openapi.diagnostic.Logger
import tabshifter.Directions.down
import tabshifter.Directions.left
import tabshifter.Directions.right
import tabshifter.valueobjects.*

open class TabShifter(private val ide: Ide) {
    /**
     * Potentially this mysterious component com.intellij.ui.switcher.SwitchManagerAppComponent
     * could be used for switching focus, but it's currently doesn't work very well and is not enabled.
     */
    open fun moveFocus(direction: Directions.Direction) {
        val layout = ide.snapshotWindowLayout()?.calculateAndSetPositions() ?: return
        val window = layout.currentWindow() ?: return
        val targetWindow = direction.findTargetWindow(window, layout) ?: return
        ide.setFocusOn(targetWindow)
    }

    /**
     * Moves tab in the specified direction.
     *
     * This is way more complicated than it should have been. The main reasons are:
     * - closing/opening or opening/closing tab doesn't guarantee that focus will be in the moved tab,
     * therefore, need to track target window to move focus into it
     * - EditorWindow object changes its identity after split/unsplit (i.e. points to another visual window)
     * therefore, need to predict target window position and look up window by expected position
     */
    open fun moveTab(direction: Directions.Direction) {
        val layout = ide.snapshotWindowLayout()?.calculateAndSetPositions() ?: return
        val window = layout.currentWindow() ?: return
        val targetWindow = direction.findTargetWindow(window, layout)

        val newPosition: Position
        val isAtEdge = targetWindow == null
        if (isAtEdge) {
            if (window.hasOneTab || !direction.canExpand()) return
            val newLayout = insertSplit(direction.splitOrientation(), window, layout)
            newLayout.calculateAndSetPositions()
            val sibling = findSiblingOf(window, newLayout) ?: return
            // should never happen
            newPosition = sibling.position
            ide.createSplitter(direction.splitOrientation())
        } else {
            val willBeUnsplit = window.hasOneTab
            if (willBeUnsplit) {
                val unsplitLayout = removeFrom(layout, window)
                unsplitLayout?.calculateAndSetPositions()
            }
            newPosition = targetWindow!!.position
            ide.openCurrentFileIn(targetWindow)
        }
        ide.closeCurrentFileIn(window) {
            val newWindowLayout = ide.snapshotWindowLayout()?.calculateAndSetPositions()
            // Do this because identity of the window object can change after closing the current file.
            val targetWindowLookedUpAgain = newWindowLayout.traverse().filterIsInstance<Window>().find { it.position == newPosition }
            if (targetWindowLookedUpAgain == null) {
                // ideally this should never happen, logging in case something goes wrong
                logger.warn("No window for: $newPosition; windowLayout: $newWindowLayout")
            } else {
                ide.setFocusOn(targetWindowLookedUpAgain)
            }
        }
    }

    open fun stretchSplitter(direction: Directions.Direction) {
        val layout = ide.snapshotWindowLayout()?.calculateAndSetPositions() ?: return
        val window = layout.currentWindow() ?: return
        var split = findParentSplitOf(window, layout)
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
        val window = layout.currentWindow()
        val split = findParentSplitOf(window, layout) ?: return

        val inFirst = split.first == window
        val maximized = ide.toggleMaximizeRestoreSplitter(split, inFirst)
        if (maximized) ide.hideToolWindows()
    }

    fun equalSizeSplitter() {
        val layout = ide.snapshotWindowLayout()?.calculateAndSetPositions() ?: return
        val split = findParentSplitOf(layout.currentWindow(), layout) ?: return
        ide.equalSizeSplitter(split)
    }

    companion object {
        private val logger = Logger.getInstance(TabShifter::class.java.name)

        private fun findParentSplitOf(layoutElement: LayoutElement?, layout: LayoutElement?): Split? {
            return layout.traverse().filterIsInstance<Split>()
                .find { it.first == layoutElement || it.second == layoutElement }
        }

        private fun LayoutElement.currentWindow() =
            traverse().filterIsInstance<Window>().find { it.isCurrent }

        private fun findSiblingOf(window: Window, element: LayoutElement): LayoutElement? {
            return when (element) {
                is Split      -> {
                    if (element.first == window) return element.second
                    if (element.second == window) return element.first
                    val first = findSiblingOf(window, element.first)
                    first ?: findSiblingOf(window, element.second)
                }
                is Window -> null
                else  -> throw IllegalStateException()
            }
        }

        private fun LayoutElement.calculateAndSetPositions(position: Position = Position(0, 0, size().width, size().height)): LayoutElement {
            if (this is Split) {
                val firstPosition: Position
                val secondPosition: Position
                if (orientation == Split.Orientation.vertical) {
                    firstPosition = position.withToX(position.toX - second.size().width)
                    secondPosition = position.withFromX(position.fromX + first.size().width)
                } else {
                    firstPosition = position.withToY(position.toY - second.size().height)
                    secondPosition = position.withFromY(position.fromY + first.size().height)
                }
                first.calculateAndSetPositions(firstPosition)
                second.calculateAndSetPositions(secondPosition)
            }
            this.position = position
            return this
        }

        private fun removeFrom(element: LayoutElement?, window: Window): LayoutElement? {
            return if (element is Split) {
                val first = removeFrom(element.first, window)
                val second = removeFrom(element.second, window)
                if (first == null) second else if (second == null) first else Split(first, second, element.orientation)
            } else if (element is Window) {
                if (element == window) null else element
            } else {
                throw IllegalStateException()
            }
        }

        private fun insertSplit(orientation: Split.Orientation, window: Window, element: LayoutElement?): LayoutElement {
            return if (element is Split) {
                Split(
                    insertSplit(orientation, window, element.first),
                    insertSplit(orientation, window, element.second),
                    element.orientation
                )
            } else if (element is Window) {
                if (element == window) Split(window, Window(hasOneTab = true, isCurrent = false), orientation) else element
            } else {
                throw IllegalStateException()
            }
        }
    }
}