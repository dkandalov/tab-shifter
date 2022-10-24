package tabshifter

import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ex.ToolWindowManagerEx
import com.intellij.ui.tabs.JBTabs
import tabshifter.layout.LayoutElement
import tabshifter.layout.Split
import tabshifter.layout.Split.Orientation
import tabshifter.layout.Split.Orientation.horizontal
import tabshifter.layout.Split.Orientation.vertical
import tabshifter.layout.Window
import java.util.*
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.SwingUtilities

class Ide(private val editorManager: FileEditorManagerEx, private val project: Project) {
    companion object {
        private val maximizeStateKey = Key.create<WeakHashMap<Splitter, MaximizeState>>("maximizeState")
    }

    // Use these particular registry values to be consistent with in com.intellij.ide.actions.WindowAction.BaseSizeAction.
    private val widthStretch: Float = Registry.intValue("ide.windowSystem.hScrollChars", 5) / 100f
    private val heightStretch: Float = Registry.intValue("ide.windowSystem.vScrollChars", 5) / 100f
    private val toolWindowManager = ToolWindowManagerEx.getInstanceEx(project)

    fun createSplitter(orientation: Orientation) {
        val swingOrientation = if (orientation == vertical) SwingConstants.VERTICAL else SwingConstants.HORIZONTAL
        editorManager.createSplitter(swingOrientation, editorManager.currentWindow)
    }

    fun closeFile(window: Window, fileUrl: String?) {
        if (fileUrl == null) return
        val virtualFile = VirtualFileManager.getInstance().findFileByUrl(fileUrl) ?: return
        val transferFocus = false // This is important for the TabShifter.moveTab() logic.
        (window as IdeWindow).editorWindow.closeFile(virtualFile, true, transferFocus)
    }

    fun openFile(window: Window, fileUrl: String?) {
        if (fileUrl == null) return
        val virtualFile = VirtualFileManager.getInstance().findFileByUrl(fileUrl) ?: return
        editorManager.openFileWithProviders(virtualFile, true, (window as IdeWindow).editorWindow)
    }

    fun setFocusOn(window: Window) {
        val editorWindow = (window as IdeWindow).editorWindow
        editorManager.currentWindow = editorWindow
        editorWindow.requestFocus(true)
    }

    fun setPinnedFiles(window: Window, pinnedFilesUrls: List<String>) {
        val editorWindow = (window as IdeWindow).editorWindow
        editorWindow.files.forEach { file ->
            if (file.url in pinnedFilesUrls) {
                editorWindow.setFilePinned(file, true)
            }
        }
    }

    fun snapshotWindowLayout(): LayoutElement? =
        if (editorManager.currentWindow == null || editorManager.currentWindow.files.isEmpty()) null
        else editorManager.snapshotWindowLayout(panel = editorManager.splitters.getComponent(0) as JPanel)

    private fun FileEditorManagerEx.snapshotWindowLayout(panel: JPanel): LayoutElement =
        when (val component = panel.getComponent(0)) {
            is Splitter -> {
                IdeSplitter(
                    first = snapshotWindowLayout(component.firstComponent as JPanel),
                    second = snapshotWindowLayout(component.secondComponent as JPanel),
                    splitter = component
                )
            }
            is JPanel, is JBTabs -> {
                val editorWindow = windows.find { window ->
                    SwingUtilities.isDescendingFrom(component, EditorWindow_AccessToPanel_Hack.panelOf(window))
                }!!
                IdeWindow(
                    editorWindow,
                    hasOneTab = editorWindow.tabCount == 1,
                    isCurrent = currentWindow == editorWindow,
                    currentFileUrl = currentFile?.url,
                    pinnedFilesUrls = editorWindow.files.filter { editorWindow.isFilePinned(it) }.map { it.url }
                )
            }
            else -> {
                throw IllegalStateException()
            }
        }

    fun growSplitProportion(split: Split) {
        updateProportion(split, 1f)
    }

    fun toggleMaximizeSplitter(split: Split, toggleFirst: Boolean): Boolean {
        val splitter = (split as IdeSplitter).splitter
        val maximizedStateByElement = project.maximizedStateByElement()

        val maximizeState = maximizedStateByElement[splitter]
        return if (maximizeState?.maximisedProportion == splitter.proportion) {
            splitter.proportion = maximizeState.originalProportion
            maximizedStateByElement.remove(splitter)
            false
        } else {
            val originalProportion = splitter.proportion
            splitter.proportion = if (toggleFirst) 1.0f else 0.0f
            maximizedStateByElement[splitter] = MaximizeState(originalProportion, maximisedProportion = splitter.proportion)
            true
        }
    }

    private fun Project.maximizedStateByElement(): WeakHashMap<Splitter, MaximizeState> {
        var result = getUserData(maximizeStateKey)
        if (result == null) {
            result = WeakHashMap()
            putUserData(maximizeStateKey, result)
        }
        return result
    }

    fun hideAllToolWindows() = toolWindowManager.let {
        it.layoutToRestoreLater = it.layout.copy()
        it.toolWindowIds.forEach { windowId ->
            it.hideToolWindow(windowId, true)
        }
        it.activateEditorComponent()
    }

    fun restoreToolWindowLayout() = toolWindowManager.let {
        val restoredLayout = it.layoutToRestoreLater
        if (restoredLayout != null) {
            it.layoutToRestoreLater = null
            it.layout = restoredLayout
        }
    }

    fun equalSizeSplitter(split: Split) {
        (split as IdeSplitter).splitter.proportion = 0.5f
    }

    fun shrinkSplitProportion(split: Split) {
        updateProportion(split, -1f)
    }

    private fun updateProportion(split: Split, direction: Float) {
        val stretch = direction * if (split.orientation == vertical) widthStretch else heightStretch
        val splitter = (split as IdeSplitter).splitter
        splitter.proportion = splitter.proportion + stretch
    }

    private class MaximizeState(val originalProportion: Float, val maximisedProportion: Float)

    private class IdeSplitter(first: LayoutElement, second: LayoutElement, val splitter: Splitter) : Split(
        first = first,
        second = second,
        orientation = if (splitter.isVertical) horizontal else vertical
    )

    private class IdeWindow(
        val editorWindow: EditorWindow,
        hasOneTab: Boolean,
        isCurrent: Boolean,
        currentFileUrl: String?,
        pinnedFilesUrls: List<String>
    ) : Window(hasOneTab, isCurrent, currentFileUrl, pinnedFilesUrls) {
        override fun toString(): String {
            val fileNames = editorWindow.files.map { it.name }
            return "Window(" + fileNames.joinToString(",") + ")"
        }
    }
}