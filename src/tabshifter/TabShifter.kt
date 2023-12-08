package tabshifter

import com.intellij.openapi.diagnostic.Logger
import tabshifter.Direction.*
import tabshifter.layout.*
import tabshifter.layout.Split.Orientation.horizontal
import tabshifter.layout.Split.Orientation.vertical
import kotlin.math.abs

class TabShifter(private val ide: Ide) {
    /**
     * Potentially this mysterious component com.intellij.ui.switcher.SwitchManagerAppComponent
     * could be used for switching focus, but it currently doesn't work very well and is not enabled.
     */
    fun moveFocus(direction: Direction) {
        val layout = ide.windowLayoutSnapshotWithPositions() ?: return
        val currentWindow = layout.findWindow { it.isCurrent } ?: return
        val targetWindow = layout.findWindowNextTo(currentWindow, direction) ?: return
        ide.setFocusOn(targetWindow)
    }

    /**
     * This is way more complicated than it should have been because
     * EditorWindow object changes its identity after split/unsplit (i.e. points to another visual window)
     * therefore, need to predict target window position and look up window by expected position
     */
    fun moveTab(direction: Direction) {
        val layout = ide.windowLayoutSnapshotWithPositions() ?: return
        val currentWindow = layout.findWindow { it.isCurrent } ?: return
        val targetWindow = layout.findWindowNextTo(currentWindow, direction)

        if (targetWindow == null) {
            if (currentWindow.hasOneTab) return

            val orientation = when (direction) {
                left, up -> return
                right    -> vertical
                down     -> horizontal
            }
            ide.createSplitter(orientation)
            val layout = ide.windowLayoutSnapshotWithPositions() ?: return
            val targetWindow = layout.findWindow { it.isCurrent } ?: return
            val currentWindow = layout.findSiblingOf(targetWindow) as Window

            layout.findWindowAt(currentWindow.position)?.let {
                ide.closeFile(it, currentWindow.currentFileUrl)
                ide.windowLayoutSnapshotWithPositions().findWindowAt(targetWindow.position)?.let { window ->
                    ide.setPinnedFiles(window, currentWindow.pinnedFilesUrls)
                }
            }
        } else {
            ide.openFile(targetWindow, currentWindow.currentFileUrl)
            ide.setPinnedFiles(targetWindow, currentWindow.pinnedFilesUrls)
            ide.closeFile(currentWindow, currentWindow.currentFileUrl)
        }
    }

    fun stretchSplitter(direction: Direction) {
        val layout = ide.windowLayoutSnapshotWithPositions() ?: return
        val currentWindow = layout.findWindow { it.isCurrent } ?: return
        var split = layout.findParentSplitOf(currentWindow)
        val orientationToSkip = if (direction == left || direction == right) horizontal else vertical
        while (split != null && split.orientation == orientationToSkip) {
            split = layout.findParentSplitOf(split)
        }
        if (split == null) return
        if (direction == right || direction == down) {
            ide.growSplitProportion(split)
        } else {
            ide.shrinkSplitProportion(split)
        }
    }

    fun toggleMaximizeSplitter() {
        val layout = ide.windowLayoutSnapshotWithPositions() ?: return
        val currentWindow = layout.findWindow { it.isCurrent } ?: return

        var isMaximized = false
        var layoutElement: LayoutElement = currentWindow
        var parentSplit = layout.findParentSplitOf(layoutElement)
        while (parentSplit != null) {
            isMaximized = ide.toggleMaximizeSplitter(parentSplit, toggleFirst = parentSplit.first == layoutElement)
            layoutElement = parentSplit
            parentSplit = layout.findParentSplitOf(layoutElement)
        }
        if (isMaximized) ide.hideAllToolWindows() else ide.restoreToolWindowLayout()
    }

    fun equalSizeSplitter() {
        val layout = ide.windowLayoutSnapshotWithPositions() ?: return
        val currentWindow = layout.findWindow { it.isCurrent } ?: return

        var layoutElement: LayoutElement = currentWindow
        var parentSplit = layout.findParentSplitOf(layoutElement)
        while (parentSplit != null) {
            ide.equalSizeSplitter(parentSplit)
            layoutElement = parentSplit
            parentSplit = layout.findParentSplitOf(layoutElement)
        }
    }
}

enum class Direction {
    left, up, right, down
}

private fun Ide.windowLayoutSnapshotWithPositions() =
    snapshotWindowLayout()?.updatePositions()

private val logger = Logger.getInstance(TabShifter::class.java.name)

private fun LayoutElement?.findWindowAt(position: Position): Window? {
    val window = findWindow { it.position == position }
    if (window == null) {
        // Haven't seen this happening. Opting for silent error handling assuming that if this goes wrong, user can just retry the action.
        logger.info("No window at: $position; windowLayout: $this")
    }
    return window
}

private fun LayoutElement.findParentSplitOf(layoutElement: LayoutElement): Split? =
    traverse().filterIsInstance<Split>()
        .find { it.first == layoutElement || it.second == layoutElement }

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

private fun LayoutElement.updatePositions(position: Position = Position(0, 0, size.width, size.height)): LayoutElement {
    if (this is Split) {
        val firstPosition: Position
        val secondPosition: Position
        if (orientation == vertical) {
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

private fun LayoutElement.findWindowNextTo(window: Window, direction: Direction) =
    when (direction) {
        left  -> filterAllWindows { window.position.fromX == it.position.toX }.minByOrNull { abs(window.position.fromY - it.position.fromY) }
        up    -> filterAllWindows { window.position.fromY == it.position.toY }.minByOrNull { abs(window.position.fromX - it.position.fromX) }
        right -> filterAllWindows { window.position.toX == it.position.fromX }.minByOrNull { abs(window.position.fromY - it.position.fromY) }
        down  -> filterAllWindows { window.position.toY == it.position.fromY }.minByOrNull { abs(window.position.fromX - it.position.fromX) }
    }

