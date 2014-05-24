package socrates.tabshift;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TabShiftActions {
    public static class ShiftLeft extends AnAction implements DumbAware {
        @Override public void actionPerformed(AnActionEvent event) {
            Project project = event.getProject();
            if (project == null) return;
            moveLeft(editorManagerIn(project), project);
        }

        private void moveLeft(FileEditorManagerEx editorManager, Project project) {
            if (editorManager.getCurrentWindow() == null) return;

            VirtualFile file = currentFileIn(project);
            int index = currentSplitIndex(editorManager);

            boolean isLeftmostSplit = (index == 0);
            if (isLeftmostSplit && editorManager.getCurrentWindow().getTabCount() == 1) return;

            editorManager.getWindows()[index].closeFile(file);
            editorManager.getWindows()[index - 1].setAsCurrentWindow(true);
            editorManager.openFile(file, true);
        }
    }

    public static class ShiftRight extends AnAction implements DumbAware {
        @Override public void actionPerformed(AnActionEvent event) {
            Project project = event.getProject();
            if (project == null) return;
            moveRight(editorManagerIn(project), project);
        }

        private void moveRight(FileEditorManagerEx editorManager, Project project) {
            if (editorManager.getCurrentWindow() == null) return;

            VirtualFile file = currentFileIn(project);
            int index = currentSplitIndex(editorManager);

            boolean isRightmostSplit = (index == editorManager.getWindows().length - 1);
            if (isRightmostSplit && editorManager.getCurrentWindow().getTabCount() == 1) return;

            if (isRightmostSplit) {
                editorManager.createSplitter(SwingConstants.VERTICAL, editorManager.getCurrentWindow());
                editorManager.getWindows()[index].closeFile(file);
                editorManager.getWindows()[index + 1].setAsCurrentWindow(true);
            } else {
                boolean wasTheOnlyTab = editorManager.getWindows()[index].getFiles().length == 1;
                int shift = (wasTheOnlyTab ? 0 : 1);
                editorManager.getWindows()[index].closeFile(file);
                editorManager.getWindows()[index + shift].setAsCurrentWindow(true);
                editorManager.openFile(file, true);
            }
        }
    }

    private static VirtualFile currentFileIn(@NotNull Project project) {
        return ((FileEditorManagerEx) FileEditorManagerEx.getInstance(project)).getCurrentFile();
    }

    private static FileEditorManagerEx editorManagerIn(Project project) {
        return FileEditorManagerEx.getInstanceEx(project);
    }

    private static int currentSplitIndex(FileEditorManagerEx fileEditorManagerEx) {
        EditorWindow[] windows = fileEditorManagerEx.getWindows();
        for (int i = 0; i < windows.length; i++) {
            if (fileEditorManagerEx.getCurrentWindow().equals(windows[i])) return i;
        }
        return -1;
    }
}
