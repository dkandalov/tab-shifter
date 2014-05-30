package tabshifter;

import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.tabs.JBTabs;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static com.intellij.openapi.fileEditor.impl.EditorWindow_AccessPanel_Hack.panelOf;
import static javax.swing.SwingUtilities.isDescendingFrom;
import static tabshifter.Split.Orientation.horizontal;
import static tabshifter.Split.Orientation.vertical;

public class Ide {
    public final FileEditorManagerEx editorManager;
    private final VirtualFile currentFile;

    public Ide(FileEditorManagerEx editorManager, Project project) {
        this.editorManager = editorManager;
        this.currentFile = currentFileIn(project);
    }

    public EditorWindow createSplitter(Split.Orientation orientation) {
        int swingOrientation = (orientation == vertical ? SwingConstants.VERTICAL : SwingConstants.HORIZONTAL);
        editorManager.createSplitter(swingOrientation, editorManager.getCurrentWindow());
        EditorWindow[] windows = editorManager.getWindows();
        return windows[windows.length - 1];
    }

    public void closeCurrentFileIn(Window window) {
        ((IdeWindow) window).editorWindow.closeFile(currentFile);
    }

    public void openCurrentFileIn(Window window) {
        editorManager.openFileWithProviders(currentFile, true, ((IdeWindow) window).editorWindow);
    }

    public void setFocusOn(Window window) {
        editorManager.setCurrentWindow(((IdeWindow) window).editorWindow);
    }

    public LayoutElement snapshotWindowLayout() {
        JPanel rootPanel = (JPanel) editorManager.getSplitters().getComponent(0);
        return snapshotWindowLayout(rootPanel);
    }

    private LayoutElement snapshotWindowLayout(JPanel panel) {
        Component component = panel.getComponent(0);

        if (component instanceof Splitter) {
            Splitter splitter = (Splitter) component;
            LayoutElement first = snapshotWindowLayout((JPanel) splitter.getFirstComponent());
            LayoutElement second = snapshotWindowLayout((JPanel) splitter.getSecondComponent());
            // note that IntelliJ Splitter has "reverse" meaning of orientation
            Split.Orientation orientation = splitter.isVertical() ? horizontal : vertical;
            return new Split(first, second, orientation);

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


    private static class IdeWindow extends Window {
        public final EditorWindow editorWindow;

        public IdeWindow(EditorWindow editorWindow, boolean hasOneTab, boolean isCurrent) {
            super(hasOneTab, isCurrent);
            this.editorWindow = editorWindow;
        }
    }
}
