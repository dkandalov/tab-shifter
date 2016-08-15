package tabshifter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;

import static tabshifter.Directions.*;

@SuppressWarnings("ComponentNotRegistered")
public class Actions {
    public static class ShiftLeft extends AnAction implements DumbAware {
	    @Override public void actionPerformed(AnActionEvent event) {
	        tabShifter(event).moveTab(left);
        }
    }
	public static class ShiftUp extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
			tabShifter(event).moveTab(up);
		}
	}
	public static class ShiftRight extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
		    tabShifter(event).moveTab(right);
	    }
    }
	public static class ShiftDown extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
		    tabShifter(event).moveTab(down);
	    }
	}

	public static class MoveFocusLeft extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
			tabShifter(event).moveFocus(left);
		}
	}
	public static class MoveFocusUp extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
			tabShifter(event).moveFocus(up);
		}
	}
	public static class MoveFocusRight extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
			tabShifter(event).moveFocus(right);
		}
	}
	public static class MoveFocusDown extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
			tabShifter(event).moveFocus(down);
		}
	}


	public static class GrowRight extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
			tabShifter(event).grow(right);
		}
	}

	public static class GrowLeft extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
			tabShifter(event).grow(left);
		}
	}

	private static TabShifter tabShifter(AnActionEvent event) {
		Project project = event.getProject();
		FileEditorManagerEx editorManager = editorManagerIn(project);
		if (project == null || editorManager == null) return TabShifter.none;

		return new TabShifter(new Ide(editorManager, project));
	}

	private static FileEditorManagerEx editorManagerIn(Project project) {
        return project == null ? null : FileEditorManagerEx.getInstanceEx(project);
    }
}
