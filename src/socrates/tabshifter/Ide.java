package socrates.tabshifter;

import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class Ide {
    private final FileEditorManagerEx editorManager;
    private final VirtualFile currentFile;

    public Ide(FileEditorManagerEx editorManager, Project project) {
        this.editorManager = editorManager;
        this.currentFile = currentFileIn(project);
    }

    public int splitCount() {
        return editorManager.getWindows().length;
    }

    public int currentSplitTabCount() {
        return editorManager.getCurrentWindow().getTabCount();
    }

    public int currentSplit() {
        EditorWindow[] windows = editorManager.getWindows();
        for (int i = 0; i < windows.length; i++) {
            if (editorManager.getCurrentWindow().equals(windows[i])) return i;
        }
        return -1;
    }

    public void createSplitter(int orientation) {
        editorManager.createSplitter(orientation, editorManager.getCurrentWindow());
    }

    public void closeCurrentFileInSplit(int splitIndex) {
        editorManager.getWindows()[splitIndex].closeFile(currentFile);
    }

    public void setFocusOnSplit(int splitIndex) {
        editorManager.getWindows()[splitIndex].setAsCurrentWindow(true);
    }

    public void reopenMovedTab() {
        editorManager.openFile(currentFile, true);
    }

    private static VirtualFile currentFileIn(@NotNull Project project) {
        return ((FileEditorManagerEx) FileEditorManagerEx.getInstance(project)).getCurrentFile();
    }
}
