package tabshifter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import java.util.HashMap;
import java.util.Map;

import static tabshifter.Directions.*;

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


	public static class StretchRight extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
			tabShifter(event).stretchSplitter(right);
		}
	}
	public static class StretchLeft extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
			tabShifter(event).stretchSplitter(left);
		}
	}
	public static class StretchUp extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
			tabShifter(event).stretchSplitter(up);
		}
	}
	public static class StretchDown extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
			tabShifter(event).stretchSplitter(down);
		}
	}

	public static class ToggleMaximizeRestore extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
			tabShifter(event).toggleMaximizeRestoreSplitter();
		}
	}

	public static class EvenSplitter extends AnAction implements DumbAware {
		@Override public void actionPerformed(AnActionEvent event) {
			tabShifter(event).evenSplitter();
		}
	}

	private static TabShifter tabShifter(AnActionEvent event) {
		Project project = event.getProject();
		if (project == null) return TabShifter.none;

		// re-use old tabShifter to preserve state
		if (projectTabShifter.containsKey(project.getProjectFilePath())) {
			return projectTabShifter.get(project.getProjectFilePath());
		}

		FileEditorManagerEx editorManager = FileEditorManagerEx.getInstanceEx(project);
		if (editorManager == null || editorManager.getAllEditors().length == 0) return TabShifter.none;

		TabShifter tabShifter = new TabShifter(new Ide(editorManager, project));
		projectTabShifter.put(project.getProjectFilePath(), tabShifter);

		Disposer.register(project, () -> projectTabShifter.remove(project.getProjectFilePath()));

		return tabShifter;
	}

	private static final Map<String, TabShifter> projectTabShifter = new HashMap<>();
}
