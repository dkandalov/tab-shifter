package tabshifter

import com.intellij.openapi.application.invokeLater
import tabshifter.valueobjects.*

class TabShifter(private val ide: Ide) {
    /**
     * Potentially this mysterious component com.intellij.ui.switcher.SwitchManagerAppComponent
     * could be used for switching focus, but it's currently doesn't work very well and is not enabled.
     */
    fun moveFocus(direction: Direction) {
        val layout = ide.windowLayoutSnapshotWithPositions() ?: return
        val currentWindow = layout.currentWindow() ?: return
        val targetWindow = direction.findTargetWindow(currentWindow, layout) ?: return
        ide.setFocusOn(targetWindow)
    }

    /**
     * Moves tab in the specified direction.
     *
     * This is way more complicated than it should have been. The main reasons are:
     * - closing-then-opening or opening-then-closing tab doesn't guarantee that focus will be in the moved tab,
     *   therefore, need to track target window to move focus into it
     * - EditorWindow object changes its identity after split/unsplit (i.e. points to another visual window)
     *   therefore, need to predict target window position and look up window by expected position
     */
    fun moveTab(direction: Direction) {
        val layout = ide.windowLayoutSnapshotWithPositions() ?: return
        val currentWindow = layout.currentWindow() ?: return
        val targetWindow = direction.findTargetWindow(currentWindow, layout)

        if (targetWindow == null) {
            if (currentWindow.hasOneTab || !direction.canExpand) return

            ide.createSplitter(direction.splitOrientation)
            val layout = ide.windowLayoutSnapshotWithPositions() ?: return
            val targetWindow = layout.currentWindow() ?: return
            val currentWindow = layout.findSiblingOf(targetWindow) as Window

            layout.findWindowAt(currentWindow.position).let {
                ide.closeFile(it, currentWindow.currentFile) {
                    ide.windowLayoutSnapshotWithPositions().findWindowAt(targetWindow.position).let {
                        invokeLater {
                            ide.setPinnedFiles(it, currentWindow.pinnedFiles)
//                            ide.setFocusOn(it) // TODO remove?
                        }
                    }
                }
            }
        } else {
            ide.closeFile(currentWindow, currentWindow.currentFile) {
                if (currentWindow.hasOneTab) {
                    // Need this for the side-effect to update currentWindow position, so the lookup below does work
                    layout.remove(currentWindow)
                }
                ide.windowLayoutSnapshotWithPositions().findWindowAt(targetWindow.position).let {
                    ide.openFile(it, currentWindow.currentFile)
                    ide.setPinnedFiles(it, currentWindow.pinnedFiles)
                }
            }
        }
    }

    private fun LayoutElement?.findWindowAt(position: Position): Window =
        traverse().filterIsInstance<Window>().find { it.position == position }
            ?: error("No window at: $position; windowLayout: $this")

    fun stretchSplitter(direction: Direction) {
        val layout = ide.windowLayoutSnapshotWithPositions() ?: return
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
        val layout = ide.windowLayoutSnapshotWithPositions() ?: return
        val currentWindow = layout.currentWindow() ?: return
        val split = findParentSplitOf(currentWindow, layout) ?: return

        val inFirst = split.first == currentWindow
        val maximized = ide.toggleMaximizeRestoreSplitter(split, inFirst)
        if (maximized) ide.hideToolWindows()
    }

    fun equalSizeSplitter() {
        val layout = ide.windowLayoutSnapshotWithPositions() ?: return
        val split = findParentSplitOf(layout.currentWindow() ?: return, layout) ?: return
        ide.equalSizeSplitter(split)
    }
}

private fun Ide.windowLayoutSnapshotWithPositions() =
    snapshotWindowLayout()?.updatePositions()

private fun findParentSplitOf(layoutElement: LayoutElement, layout: LayoutElement): Split? =
    layout.traverse().filterIsInstance<Split>()
        .find { it.first == layoutElement || it.second == layoutElement }

private fun LayoutElement.currentWindow() =
    traverse().filterIsInstance<Window>().find { it.isCurrent }

private fun LayoutElement.findSiblingOf(window: Window): LayoutElement? =
    when (this) {
        is Split  -> when {
            first == window  -> second
            second == window -> first
            else             -> first.findSiblingOf(window) ?: second.findSiblingOf(window)
        }
        is Window -> null
        else      -> throw IllegalStateException()
    }

private fun LayoutElement.remove(window: Window): LayoutElement? {
    fun LayoutElement.doRemove(window: Window): LayoutElement? =
        when (this) {
            is Split  -> {
                val first = first.doRemove(window)
                val second = second.doRemove(window)
                when {
                    first == null  -> second
                    second == null -> first
                    else           -> Split(first, second, this.orientation)
                }
            }
            is Window -> if (this == window) null else this
            else      -> throw IllegalStateException()
        }

    return doRemove(window)?.updatePositions()
}

private fun LayoutElement.insertSplit(orientation: Split.Orientation, window: Window): LayoutElement {
    fun LayoutElement.doInsertSplit(orientation: Split.Orientation, window: Window): LayoutElement =
        when (this) {
            is Split  -> Split(
                first.doInsertSplit(orientation, window),
                second.doInsertSplit(orientation, window),
                orientation
            )
            is Window ->
                if (this != window) this
                else Split(
                    window,
                    Window(hasOneTab = true, isCurrent = false, currentFile = currentFile, pinnedFiles = emptyList()),
                    orientation
                )
            else      -> throw IllegalStateException()
        }

    return doInsertSplit(orientation, window).updatePositions()
}

private fun LayoutElement.updatePositions(position: Position = Position(0, 0, size.width, size.height)): LayoutElement {
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
        first.updatePositions(firstPosition)
        second.updatePositions(secondPosition)
    }
    this.position = position
    return this
}
