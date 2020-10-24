package tabshifter;

import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.ex.*;
import com.intellij.openapi.fileEditor.impl.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.util.registry.*;
import com.intellij.openapi.util.text.*;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.wm.ex.*;
import com.intellij.ui.tabs.*;
import com.intellij.util.messages.*;
import org.jetbrains.annotations.*;
import tabshifter.valueobjects.Window;
import tabshifter.valueobjects.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static javax.swing.SwingUtilities.*;
import static tabshifter.EditorWindow_AccessToPanel_Hack.*;
import static tabshifter.valueobjects.Split.Orientation.*;

public class Ide {
	private final FileEditorManagerEx editorManager;
	private final Project project;
	private final float widthStretch;
	private final float heightStretch;
	private final ToolWindowManagerEx toolWindowManager;
	private MaximizeState maximizeState;

	public Ide(FileEditorManagerEx editorManager, Project project) {
		this.editorManager = editorManager;
		this.toolWindowManager = ToolWindowManagerEx.getInstanceEx(project);
		this.project = project;
		this.maximizeState = null;

		// Use these particular registry values to be consistent with in com.intellij.ide.actions.WindowAction.BaseSizeAction.
		this.widthStretch = Registry.intValue("ide.windowSystem.hScrollChars", 5) / 100f;
		this.heightStretch = Registry.intValue("ide.windowSystem.vScrollChars", 5) / 100f;
	}

	public void createSplitter(Split.Orientation orientation) {
		int swingOrientation = (orientation == vertical ? SwingConstants.VERTICAL : SwingConstants.HORIZONTAL);
		editorManager.createSplitter(swingOrientation, editorManager.getCurrentWindow());
		editorManager.getWindows();
	}

	public void closeCurrentFileIn(Window window, Runnable onFileClosed) {
		VirtualFile fileToClose = currentFileIn(project);

		MessageBusConnection connection = project.getMessageBus().connect();
		connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
			@Override public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
				if (file.equals(fileToClose)) {
					onFileClosed.run();
					connection.disconnect();
				}
			}
		});

		((IdeWindow) window).editorWindow.closeFile(fileToClose);
	}

	public void openCurrentFileIn(Window window) {
		editorManager.openFileWithProviders(currentFileIn(project), true, ((IdeWindow) window).editorWindow);
	}

	public void setFocusOn(Window window) {
		editorManager.setCurrentWindow(((IdeWindow) window).editorWindow);
	}

	public LayoutElement snapshotWindowLayout() {
		JPanel rootPanel = (JPanel) editorManager.getSplitters().getComponent(0);
		return snapshotWindowLayout(rootPanel);
	}

	private LayoutElement snapshotWindowLayout(JPanel panel) {
		if (editorManager.getCurrentWindow() == null || editorManager.getCurrentWindow().getFiles().length == 0) {
			return LayoutElement.none;
		}
		Component component = panel.getComponent(0);

		if (component instanceof Splitter) {
			Splitter splitter = (Splitter) component;
			LayoutElement first = snapshotWindowLayout((JPanel) splitter.getFirstComponent());
			LayoutElement second = snapshotWindowLayout((JPanel) splitter.getSecondComponent());
			return new IdeSplitter(first, second, splitter);

		} else if (component instanceof JPanel || component instanceof JBTabs) {
			EditorWindow editorWindow = findWindowWith(component);
			boolean hasOneTab = (editorWindow.getTabCount() == 1);
			boolean isCurrent = editorManager.getCurrentWindow().equals(editorWindow);
			return new IdeWindow(editorWindow, hasOneTab, isCurrent);
		} else {
			throw new IllegalStateException();
		}
	}

	private EditorWindow findWindowWith(Component component) {
		if (component == null) return null;

		for (EditorWindow window : editorManager.getWindows()) {
			if (isDescendingFrom(component, panelOf(window))) {
				return window;
			}
		}
		return null;
	}

	private static VirtualFile currentFileIn(@NotNull Project project) {
		return ((FileEditorManagerEx) FileEditorManagerEx.getInstance(project)).getCurrentFile();
	}

	public void growSplitProportion(Split split) {
		updateProportion(split, 1);
	}

	private static class MaximizeState {
		float oldProportion;
		float newProportion;
	}

	public boolean toggleMaximizeRestoreSplitter(Split split, boolean inFirst) {
		Splitter splitter = ((IdeSplitter) split).splitter;

		// zoom out if the proportion equals the one during maximization
		if (this.maximizeState != null && this.maximizeState.newProportion == splitter.getProportion()) {
			splitter.setProportion(this.maximizeState.oldProportion);
			this.maximizeState = null;
			return false;
		}

		// maximize
		this.maximizeState = new MaximizeState();
		this.maximizeState.oldProportion = splitter.getProportion();
		splitter.setProportion(inFirst ? 1.0F : 0.0F);
		this.maximizeState.newProportion = splitter.getProportion();
		return true;
	}

	public void evenSplitter(Split split) {
		Splitter splitter = ((IdeSplitter) split).splitter;

		// equal
		this.maximizeState = new MaximizeState();
		this.maximizeState.oldProportion = splitter.getProportion();
		splitter.setProportion(0.5F);
		this.maximizeState.newProportion = splitter.getProportion();
	}

	public void hideToolWindows() {
		for (String windowId : toolWindowManager.getToolWindowIds()) {
			toolWindowManager.hideToolWindow(windowId, true);
		}
	}

	public void shrinkSplitProportion(Split split) {
		updateProportion(split, -1);
	}

	private void updateProportion(Split split, float direction) {
		float stretch = direction * (split.orientation == vertical ? widthStretch : heightStretch);
		Splitter splitter = ((IdeSplitter) split).splitter;
		splitter.setProportion(splitter.getProportion() + stretch);
	}


	private static class IdeSplitter extends Split {
		public final Splitter splitter;

		public IdeSplitter(LayoutElement first, LayoutElement second, Splitter splitter) {
			super(first, second, splitter.isVertical() ? horizontal : vertical);
			this.splitter = splitter;
		}
	}

	private static class IdeWindow extends Window {
		public final EditorWindow editorWindow;

		public IdeWindow(EditorWindow editorWindow, boolean hasOneTab, boolean isCurrent) {
			super(hasOneTab, isCurrent);
			this.editorWindow = editorWindow;
		}

		@Override public String toString() {
			Collection<String> fileNames = new ArrayList<>();
			for (VirtualFile virtualFile : editorWindow.getFiles()) {
				fileNames.add(virtualFile.getName());
			}
			return "Window(" + StringUtil.join(fileNames, ",") + ")";
		}
	}
}
