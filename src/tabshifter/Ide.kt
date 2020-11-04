package tabshifter

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ex.ToolWindowManagerEx
import com.intellij.ui.tabs.JBTabs
import tabshifter.valueobjects.LayoutElement
import tabshifter.valueobjects.Split
import tabshifter.valueobjects.Window
import java.awt.Component
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.SwingUtilities

class Ide(private val editorManager: FileEditorManagerEx, private val project: Project) {
    private val maximizeStateKey = Key.create<MaximizeState>("maximizeState")

    // Use these particular registry values to be consistent with in com.intellij.ide.actions.WindowAction.BaseSizeAction.
    private val widthStretch: Float = Registry.intValue("ide.windowSystem.hScrollChars", 5) / 100f
    private val heightStretch: Float = Registry.intValue("ide.windowSystem.vScrollChars", 5) / 100f
    private val toolWindowManager: ToolWindowManagerEx = ToolWindowManagerEx.getInstanceEx(project)

    fun createSplitter(orientation: Split.Orientation) {
        val swingOrientation = if (orientation == Split.Orientation.vertical) SwingConstants.VERTICAL else SwingConstants.HORIZONTAL
        editorManager.createSplitter(swingOrientation, editorManager.currentWindow)
        editorManager.windows
    }

    fun closeCurrentFileIn(window: Window, onFileClosed: () -> Unit) {
        val fileToClose = project.currentFile()
        val connection = project.messageBus.connect()
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object: FileEditorManagerListener {
            override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                if (file == fileToClose) {
                    onFileClosed()
                    connection.disconnect()
                }
            }
        })
        (window as IdeWindow).editorWindow!!.closeFile(fileToClose)
    }

    fun openCurrentFileIn(window: Window) {
        editorManager.openFileWithProviders(project.currentFile()!!, true, (window as IdeWindow).editorWindow!!)
    }

    fun setFocusOn(window: Window) {
        editorManager.currentWindow = (window as IdeWindow).editorWindow
    }

    fun snapshotWindowLayout(): LayoutElement? {
        val rootPanel = editorManager.splitters.getComponent(0) as JPanel
        if (editorManager.currentWindow == null || editorManager.currentWindow.files.isEmpty()) return null
        return snapshotWindowLayout(rootPanel)
    }

    private fun snapshotWindowLayout(panel: JPanel): LayoutElement {
        val component = panel.getComponent(0)
        return if (component is Splitter) {
            IdeSplitter(
                first = snapshotWindowLayout(component.firstComponent as JPanel),
                second = snapshotWindowLayout(component.secondComponent as JPanel),
                splitter = component
            )
        } else if (component is JPanel || component is JBTabs) {
            val editorWindow = findWindowWith(component)
            IdeWindow(
                editorWindow,
                hasOneTab = editorWindow!!.tabCount == 1,
                isCurrent = editorManager.currentWindow == editorWindow
            )
        } else {
            throw IllegalStateException()
        }
    }

    private fun findWindowWith(component: Component?): EditorWindow? {
        if (component == null) return null
        return editorManager.windows
            .find { window -> SwingUtilities.isDescendingFrom(component, EditorWindow_AccessToPanel_Hack.panelOf(window)) }
    }

    fun growSplitProportion(split: Split) {
        updateProportion(split, 1f)
    }

    fun toggleMaximizeRestoreSplitter(split: Split, inFirst: Boolean): Boolean {
        val splitter = (split as IdeSplitter).splitter

        // zoom out if the proportion equals the one during maximization
        val maximizeState = project.getUserData(maximizeStateKey)
        if (maximizeState != null && maximizeState.maximisedProportion == splitter.proportion) {
            splitter.proportion = maximizeState.originalProportion
            project.putUserData(maximizeStateKey, null)
            return false
        }
        val originalProportion = splitter.proportion
        splitter.proportion = if (inFirst) 1.0f else 0.0f
        val maximisedProportion = splitter.proportion
        project.putUserData(maximizeStateKey, MaximizeState(originalProportion, maximisedProportion))
        return true
    }

    fun equalSizeSplitter(split: Split) {
        (split as IdeSplitter).splitter.proportion = 0.5f
    }

    fun hideToolWindows() {
        toolWindowManager.toolWindowIds.forEach { windowId ->
            toolWindowManager.hideToolWindow(windowId, true)
        }
    }

    fun shrinkSplitProportion(split: Split) {
        updateProportion(split, -1f)
    }

    private fun updateProportion(split: Split, direction: Float) {
        val stretch = direction * if (split.orientation == Split.Orientation.vertical) widthStretch else heightStretch
        val splitter = (split as IdeSplitter).splitter
        splitter.proportion = splitter.proportion + stretch
    }

    private class MaximizeState(val originalProportion: Float, val maximisedProportion: Float)

    private class IdeSplitter(first: LayoutElement, second: LayoutElement, val splitter: Splitter):
        Split(
            first = first,
            second = second,
            orientation = if (splitter.isVertical) Orientation.horizontal else Orientation.vertical
        )

    private class IdeWindow(val editorWindow: EditorWindow?, hasOneTab: Boolean, isCurrent: Boolean): Window(hasOneTab, isCurrent) {
        override fun toString(): String {
            val fileNames = editorWindow!!.files.map { it.name }
            return "Window(" + fileNames.joinToString(",") + ")"
        }
    }

    private fun Project.currentFile(): VirtualFile? {
        return (FileEditorManagerEx.getInstance(this) as FileEditorManagerEx).currentFile
    }
}