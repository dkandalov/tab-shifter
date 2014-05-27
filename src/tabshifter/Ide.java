package tabshifter;

import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.EditorWindowHack;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.tabs.JBTabs;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.max;
import static javax.swing.SwingUtilities.isDescendingFrom;

public class Ide {
    private final FileEditorManagerEx editorManager;
    private final VirtualFile currentFile;

    public Ide(FileEditorManagerEx editorManager, Project project) {
        this.editorManager = editorManager;
        this.currentFile = currentFileIn(project);
    }

    public int currentSplitTabCount() {
        return editorManager.getCurrentWindow().getTabCount();
    }

    public EditorWindow createSplitter(int orientation) {
        editorManager.createSplitter(orientation, editorManager.getCurrentWindow());
        EditorWindow[] windows = editorManager.getWindows();
        return windows[windows.length - 1];
    }

    public void closeCurrentFileIn(EditorWindow editorWindow) {
        editorWindow.closeFile(currentFile);
    }

    public void setFocusOn(EditorWindow editorWindow) {
        editorWindow.setAsCurrentWindow(true);
    }

    public void reopenMovedTab() {
        editorManager.openFile(currentFile, true);
    }

    public void reopenFileIn(EditorWindow editorWindow) {
        editorManager.openFileWithProviders(currentFile, true, editorWindow);
    }

    private static VirtualFile currentFileIn(@NotNull Project project) {
        return ((FileEditorManagerEx) FileEditorManagerEx.getInstance(project)).getCurrentFile();
    }

    public Size layoutSize() {
        if (editorManager.getSplitters().getComponentCount() == 0) return new Size(0, 0);

        JPanel root = (JPanel) editorManager.getSplitters().getComponent(0);
        Map<Component, Size> sizeByComponent = sizeOfSplitLayout(root);
        return sizeByComponent.get(root.getComponent(0));
    }

    public EditorWindow currentWindow() {
        return editorManager.getCurrentWindow();
    }

    public Map<EditorWindow, Position> windowPositions() {
        if (editorManager.getSplitters().getComponentCount() == 0) {
            return new HashMap<EditorWindow, Position>();
        }

        JPanel root = (JPanel) editorManager.getSplitters().getComponent(0);
        Map<Component, Size> sizeByComponent = sizeOfSplitLayout(root);

        Size rootSize = sizeByComponent.get(root.getComponent(0));
        return positionsOfWindows(root, new Position(0, 0, rootSize.width, rootSize.height), sizeByComponent);
    }

    private Map<EditorWindow, Position> positionsOfWindows(JPanel panel, Position position, Map<Component, Size> sizeByComponent) {
        Component component = panel.getComponent(0);
        if (component instanceof Splitter) {
            Splitter splitter = (Splitter) component;

            Size firstSize = sizeByComponent.get(splitter.getFirstComponent().getComponent(0));
            Size secondSize = sizeByComponent.get(splitter.getSecondComponent().getComponent(0));

            Position firstPosition;
            Position secondPosition;
            if (splitter.isVertical()) {
                firstPosition = position.withToY(position.toY - secondSize.height);
                secondPosition = position.withFromY(position.fromY + firstSize.height);
            } else {
                firstPosition = position.withToX(position.toX - secondSize.width);
                secondPosition = position.withFromX(position.fromX + firstSize.width);
            }
            Map<EditorWindow, Position> first = positionsOfWindows((JPanel) splitter.getFirstComponent(), firstPosition, sizeByComponent);
            Map<EditorWindow, Position> second = positionsOfWindows((JPanel) splitter.getSecondComponent(), secondPosition, sizeByComponent);

            Map<EditorWindow, Position> result = new HashMap<EditorWindow, Position>();
            result.putAll(first);
            result.putAll(second);
            return result;

        } else if (component instanceof JPanel || component instanceof JBTabs) {
            Map<EditorWindow, Position> result = new HashMap<EditorWindow, Position>();
            result.put(findWindowWith(component), position);
            return result;
        } else {
            throw new IllegalStateException();
        }
    }

    private static Map<Component, Size> sizeOfSplitLayout(JPanel panel) {
        Component component = panel.getComponent(0);
        if (component instanceof Splitter) {
            Splitter splitter = (Splitter) component;
            Map<Component, Size> first = sizeOfSplitLayout((JPanel) splitter.getFirstComponent());
            Map<Component, Size> second = sizeOfSplitLayout((JPanel) splitter.getSecondComponent());

            Size firstSize = first.get(splitter.getFirstComponent().getComponent(0));
            Size secondSize = second.get(splitter.getSecondComponent().getComponent(0));
            Size size = splitter.isVertical() ?
                    new Size(max(firstSize.width, secondSize.width), firstSize.height + secondSize.height) :
                    new Size(firstSize.width + secondSize.width, max(firstSize.height, secondSize.height));

            Map<Component, Size> result = new HashMap<Component, Size>();
            result.put(component, size);
            result.putAll(first);
            result.putAll(second);
            return result;
        } else if (component instanceof JPanel || component instanceof JBTabs) {
            Map<Component, Size> result = new HashMap<Component, Size>();
            result.put(component, new Size(1, 1));
            return result;
        } else {
            throw new IllegalStateException();
        }
    }

    private EditorWindow findWindowWith(Component component) {
        if (component == null) return null;

        for (EditorWindow window : editorManager.getWindows()) {
            if (isDescendingFrom(component, EditorWindowHack.panelOf(window))) {
                return window;
            }
        }
        return null;
    }

}
