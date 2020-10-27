package tabshifter;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.ex.*;
import com.intellij.openapi.project.*;
import org.jetbrains.annotations.*;

import static tabshifter.Directions.*;

public class Actions {
	public static class ShiftLeft extends AnAction implements DumbAware {
		@Override public void actionPerformed(@NotNull AnActionEvent event) {
			tabShifter(event).moveTab(left);
		}
	}
	public static class ShiftUp extends AnAction implements DumbAware {
		@Override public void actionPerformed(@NotNull AnActionEvent event) {
			tabShifter(event).moveTab(up);
		}
	}
	public static class ShiftRight extends AnAction implements DumbAware {
		@Override public void actionPerformed(@NotNull AnActionEvent event) {
			tabShifter(event).moveTab(right);
		}
	}
	public static class ShiftDown extends AnAction implements DumbAware {
		@Override public void actionPerformed(@NotNull AnActionEvent event) {
			tabShifter(event).moveTab(down);
		}
	}

	public static class MoveFocusLeft extends AnAction implements DumbAware {
		@Override public void actionPerformed(@NotNull AnActionEvent event) {
			tabShifter(event).moveFocus(left);
		}
	}
	public static class MoveFocusUp extends AnAction implements DumbAware {
		@Override public void actionPerformed(@NotNull AnActionEvent event) {
			tabShifter(event).moveFocus(up);
		}
	}
	public static class MoveFocusRight extends AnAction implements DumbAware {
		@Override public void actionPerformed(@NotNull AnActionEvent event) {
			tabShifter(event).moveFocus(right);
		}
	}
	public static class MoveFocusDown extends AnAction implements DumbAware {
		@Override public void actionPerformed(@NotNull AnActionEvent event) {
			tabShifter(event).moveFocus(down);
		}
	}


	public static class StretchRight extends AnAction implements DumbAware {
		@Override public void actionPerformed(@NotNull AnActionEvent event) {
			tabShifter(event).stretchSplitter(right);
		}
	}
	public static class StretchLeft extends AnAction implements DumbAware {
		@Override public void actionPerformed(@NotNull AnActionEvent event) {
			tabShifter(event).stretchSplitter(left);
		}
	}
	public static class StretchUp extends AnAction implements DumbAware {
		@Override public void actionPerformed(@NotNull AnActionEvent event) {
			tabShifter(event).stretchSplitter(up);
		}
	}
	public static class StretchDown extends AnAction implements DumbAware {
		@Override public void actionPerformed(@NotNull AnActionEvent event) {
			tabShifter(event).stretchSplitter(down);
		}
	}

	public static class EqualSizeSplit extends AnAction implements DumbAware {
		@Override public void actionPerformed(@NotNull AnActionEvent event) {
			tabShifter(event).equalSizeSplitter();
		}
	}

	public static class ToggleMaximizeRestore extends AnAction implements DumbAware {
		@Override public void actionPerformed(@NotNull AnActionEvent event) {
			tabShifter(event).toggleMaximizeRestoreSplitter();
		}
	}

	private static TabShifter tabShifter(AnActionEvent event) {
		Project project = event.getProject();
		if (project == null) return TabShifter.none;

		FileEditorManagerEx editorManager = FileEditorManagerEx.getInstanceEx(project);
		if (editorManager == null || editorManager.getAllEditors().length == 0) return TabShifter.none;

		return new TabShifter(new Ide(editorManager, project));
	}
}
