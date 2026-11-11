package tabshifter

import com.intellij.openapi.fileEditor.FileEditorManagerKeys
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.EditorWindow
import com.intellij.openapi.fileEditor.impl.FileEditorOpenOptions
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ex.ToolWindowManagerEx
import com.intellij.ui.tabs.JBTabs
import tabshifter.layout.LayoutElement
import tabshifter.layout.Split
import tabshifter.layout.Split.Orientation
import tabshifter.layout.Split.Orientation.horizontal
import tabshifter.layout.Split.Orientation.vertical
import tabshifter.layout.Window
import java.awt.Container
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

    fun moveFile(from: Window, to: Window, fileUrl: String?) {
        val sourceWindow = (from as IdeWindow).editorWindow
        val targetWindow = (to as IdeWindow).editorWindow
        val file = sourceWindow.findFile(fileUrl) ?: return
        closingToReopen(file) {
            val isPinned = sourceWindow.isFilePinned(file)
            sourceWindow.closeFile(file, true, false)
            @Suppress("UnstableApiUsage")
            editorManager.openFile(
                file,
                targetWindow,
                FileEditorOpenOptions().withSelectAsCurrent(true).withRequestFocus(true).withReuseOpen(true)
            )
            if (isPinned) targetWindow.setFilePinned(file, true)
        }
        setFocusOn(to)
    }

    /**
     * The file is closed before splitting (rather than letting split() copy it and closing the original afterwards)
     * because editors of files marked with FileEditorManagerKeys.FORBID_TAB_SPLIT (e.g. agent sessions)
     * must never be open in two windows at once.
     */
    fun moveFileToNewSplitter(from: Window, fileUrl: String?, orientation: Orientation) {
        val sourceWindow = (from as IdeWindow).editorWindow
        val file = sourceWindow.findFile(fileUrl) ?: return
        val swingOrientation = if (orientation == vertical) SwingConstants.VERTICAL else SwingConstants.HORIZONTAL
        closingToReopen(file) {
            val isPinned = sourceWindow.isFilePinned(file)
            sourceWindow.closeFile(file, true, false)
            val newWindow = sourceWindow.split(
                orientation = swingOrientation,
                forceSplit = true,
                virtualFile = file,
                focusNew = true
            ) ?: return
            if (isPinned) newWindow.setFilePinned(file, true)
        }
    }

    /**
     * Uses the exact VirtualFile instance open in the window instead of resolving the url through VirtualFileManager,
     * because non-standard editors (e.g. agent sessions) are backed by custom file systems
     * which may not resolve the url or may resolve it to a different instance.
     */
    private fun EditorWindow.findFile(fileUrl: String?): VirtualFile? =
        if (fileUrl == null) null else fileList.find { it.url == fileUrl }

    /**
     * Marks the file as being reopened so that editors which release their state on close (e.g. agent sessions)
     * keep it while the file is moved. See com.intellij.openapi.fileEditor.impl.MoveEditorToOppositeTabGroupAction.
     */
    private inline fun closingToReopen(file: VirtualFile, f: () -> Unit) {
        file.putUserData(FileEditorManagerKeys.CLOSING_TO_REOPEN, true)
        try {
            f()
        } finally {
            file.putUserData(FileEditorManagerKeys.CLOSING_TO_REOPEN, null)
        }
    }

    fun setFocusOn(window: Window) {
        val editorWindow = (window as IdeWindow).editorWindow
        editorManager.currentWindow = editorWindow
        editorWindow.requestFocus(true)
    }

    fun snapshotWindowLayout(): LayoutElement? =
        if (editorManager.currentWindow == null || editorManager.currentWindow!!.fileList.isEmpty()) null
        else editorManager.snapshotWindowLayout(container = editorManager.splitters.getComponent(0) as Container)

    private fun FileEditorManagerEx.snapshotWindowLayout(container: Container): LayoutElement =
        when (container) {
            is Splitter -> {
                IdeSplitter(
                    first = snapshotWindowLayout(container.firstComponent),
                    second = snapshotWindowLayout(container.secondComponent),
                    splitter = container
                )
            }
            is JPanel, is JBTabs -> {
                val editorWindow = windows.find { window ->
                    SwingUtilities.isDescendingFrom(container, EditorWindow_AccessToPanel_Hack.panelOf(window))
                }!!
                IdeWindow(
                    editorWindow,
                    hasOneTab = editorWindow.tabCount == 1,
                    isCurrent = currentWindow == editorWindow,
                    currentFileUrl = currentFile?.url
                )
            }
            else -> {
                error("Unexpected container ${container.javaClass.name}")
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

    @Suppress("UnstableApiUsage")
    fun hideAllToolWindows() = toolWindowManager.let {
        it.layoutToRestoreLater = it.getLayout().copy()
        it.toolWindowIds.forEach { windowId ->
            it.hideToolWindow(windowId, true)
        }
        it.activateEditorComponent()
    }

    @Suppress("UnstableApiUsage")
    fun restoreToolWindowLayout() = toolWindowManager.let {
        val restoredLayout = it.layoutToRestoreLater
        if (restoredLayout != null) {
            it.layoutToRestoreLater = null
            it.setLayout(restoredLayout)
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
        splitter.proportion += stretch
    }

    private class MaximizeState(val originalProportion: Float, val maximisedProportion: Float)

    private class IdeSplitter(first: LayoutElement, second: LayoutElement, val splitter: Splitter) :
        Split(
            first = first,
            second = second,
            orientation = if (splitter.isVertical) horizontal else vertical
        )

    private class IdeWindow(
        val editorWindow: EditorWindow,
        hasOneTab: Boolean,
        isCurrent: Boolean,
        currentFileUrl: String?
    ) : Window(hasOneTab, isCurrent, currentFileUrl) {
        override fun toString() =
            "Window(" + editorWindow.fileList.joinToString(",") { it.name } + ")" +
                if (isCurrent) "*" else ""
    }
}