package tabshifter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;

@SuppressWarnings("ComponentNotRegistered")
public class Actions {
    public static class ShiftLeft extends AnAction implements DumbAware {
        @Override public void actionPerformed(AnActionEvent event) {
            Project project = event.getProject();
            FileEditorManagerEx editorManager = editorManagerIn(project);
            if (project == null || editorManager == null) return;

	        new TabShifter(new Ide(editorManager, project)).moveTabLeft();
        }
    }
	public static class ShiftUp extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
			Project project = event.getProject();
			FileEditorManagerEx editorManager = editorManagerIn(project);
			if (project == null || editorManager == null) return;

			new TabShifter(new Ide(editorManager, project)).moveTabUp();
		}
	}
	public static class ShiftRight extends AnAction implements DumbAware {
	    @Override public void actionPerformed(AnActionEvent event) {
            Project project = event.getProject();
            FileEditorManagerEx editorManager = editorManagerIn(project);
            if (project == null || editorManager == null) return;

	        new TabShifter(new Ide(editorManager, project)).moveTabRight();
        }

    }
	public static class ShiftDown extends AnAction implements DumbAware {
	    @Override public void actionPerformed(AnActionEvent event) {
            Project project = event.getProject();
            FileEditorManagerEx editorManager = editorManagerIn(project);
            if (project == null || editorManager == null) return;

	        new TabShifter(new Ide(editorManager, project)).moveTabDown();
        }

    }

	public static class MoveFocusLeft extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
			Project project = event.getProject();
			FileEditorManagerEx editorManager = editorManagerIn(project);
			if (project == null || editorManager == null) return;

			Ide ide = new Ide(editorManager, project);
			new TabShifter(ide).moveFocusLeft();
		}
	}

	private static FileEditorManagerEx editorManagerIn(Project project) {
        return project == null ? null : FileEditorManagerEx.getInstanceEx(project);
    }
}
