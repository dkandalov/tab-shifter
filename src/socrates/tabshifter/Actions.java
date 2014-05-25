package socrates.tabshifter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;

public class Actions {
    public static class ShiftLeft extends AnAction implements DumbAware {
        @Override public void actionPerformed(AnActionEvent event) {
            Project project = event.getProject();
            FileEditorManagerEx editorManager = editorManagerIn(project);
            if (project == null || editorManager == null) return;

            Ide ide = new Ide(editorManager, project);
            new TabShifter(ide).moveTabLeft();
        }
    }

    public static class ShiftRight extends AnAction implements DumbAware {
        @Override public void actionPerformed(AnActionEvent event) {
            Project project = event.getProject();
            FileEditorManagerEx editorManager = editorManagerIn(project);
            if (project == null || editorManager == null) return;

            Ide ide = new Ide(editorManager, project);
            new TabShifter(ide).moveTabRight();
        }
    }

    private static FileEditorManagerEx editorManagerIn(Project project) {
        return project == null ? null : FileEditorManagerEx.getInstanceEx(project);
    }
}
