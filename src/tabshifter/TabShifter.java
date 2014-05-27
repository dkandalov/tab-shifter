package tabshifter;

import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.util.Condition;

import javax.swing.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.intellij.util.containers.ContainerUtil.findAll;
import static com.intellij.util.containers.ContainerUtil.sort;

public class TabShifter {
    private final Ide ide;

    public TabShifter(Ide ide) {
        this.ide = ide;
    }

    public void moveTabLeft() {
        EditorWindow window = ide.currentWindow();
        Map<EditorWindow, Position> windowPositions = ide.windowPositions();
        Position position = windowPositions.get(window);

        boolean isLeftmostWindow = (position.fromX == 0);
        if (isLeftmostWindow) return;
        boolean isTheOnlyTab = window.getTabCount() == 1;

        EditorWindow leftWindow = findWindowLeftOf(position, windowPositions);
        ide.reopenFileIn(leftWindow);
        ide.closeCurrentFileIn(window);
        if (!isTheOnlyTab) ide.setFocusOn(leftWindow);
    }

    public void moveTabUp() {
        EditorWindow window = ide.currentWindow();
        Map<EditorWindow, Position> windowPositions = ide.windowPositions();
        Position position = windowPositions.get(window);

        boolean isTopWindow = (position.fromY == 0);
        if (isTopWindow) return;
        boolean isTheOnlyTab = window.getTabCount() == 1;

        EditorWindow windowAbove = findWindowAbove(position, windowPositions);
        ide.reopenFileIn(windowAbove);
        ide.closeCurrentFileIn(window);
        if (!isTheOnlyTab) ide.setFocusOn(windowAbove);
    }

    public void moveTabRight() {
        Size layoutSize = ide.layoutSize();
        EditorWindow window = ide.currentWindow();
        Map<EditorWindow, Position> windowPositions = ide.windowPositions();
        Position position = windowPositions.get(window);

        boolean isRightmostWindow = (position.toX == layoutSize.width);
        if (isRightmostWindow && ide.currentSplitTabCount() == 1) return;

        if (isRightmostWindow) {
            EditorWindow newWindow = ide.createSplitter(SwingConstants.VERTICAL);
            ide.closeCurrentFileIn(window);
            ide.setFocusOn(newWindow);
        } else {
            boolean isTheOnlyTab = window.getTabCount() == 1;
            EditorWindow rightWindow = findWindowRightOf(position, windowPositions);
            ide.reopenFileIn(rightWindow);
            ide.closeCurrentFileIn(window);
            if (!isTheOnlyTab) ide.setFocusOn(rightWindow);
        }
    }

    public void moveTabDown() {
        Size layoutSize = ide.layoutSize();
        EditorWindow window = ide.currentWindow();
        Map<EditorWindow, Position> windowPositions = ide.windowPositions();
        Position position = windowPositions.get(window);

        boolean isBottomWindow = (position.toY == layoutSize.height);
        if (isBottomWindow && ide.currentSplitTabCount() == 1) return;

        if (isBottomWindow) {
            EditorWindow newWindow = ide.createSplitter(SwingConstants.HORIZONTAL);
            ide.closeCurrentFileIn(window);
            ide.setFocusOn(newWindow);
        } else {
            boolean isTheOnlyTab = window.getTabCount() == 1;
            EditorWindow windowBelow = findWindowBelow(position, windowPositions);
            ide.reopenFileIn(windowBelow);
            ide.closeCurrentFileIn(window);
            if (!isTheOnlyTab) ide.setFocusOn(windowBelow);
        }
    }

    private static EditorWindow findWindowRightOf(final Position position, Map<EditorWindow, Position> windowPositions) {
        List<Map.Entry<EditorWindow, Position>> nextWindows = findAll(windowPositions.entrySet(), new Condition<Map.Entry<EditorWindow, Position>>() {
            @Override
            public boolean value(Map.Entry<EditorWindow, Position> entry) {
                return position.toX == entry.getValue().fromX;
            }
        });
        sort(nextWindows, new Comparator<Map.Entry<EditorWindow, Position>>() {
            @Override
            public int compare(Map.Entry<EditorWindow, Position> o1, Map.Entry<EditorWindow, Position> o2) {
                return Double.compare(o1.getValue().fromY, o2.getValue().fromY);
            }
        });
        return nextWindows.get(0).getKey();
    }

    private static EditorWindow findWindowBelow(final Position position, Map<EditorWindow, Position> windowPositions) {
        List<Map.Entry<EditorWindow, Position>> nextWindows = findAll(windowPositions.entrySet(), new Condition<Map.Entry<EditorWindow, Position>>() {
            @Override
            public boolean value(Map.Entry<EditorWindow, Position> entry) {
                return position.toY == entry.getValue().fromY;
            }
        });
        sort(nextWindows, new Comparator<Map.Entry<EditorWindow, Position>>() {
            @Override
            public int compare(Map.Entry<EditorWindow, Position> o1, Map.Entry<EditorWindow, Position> o2) {
                return Double.compare(o1.getValue().fromX, o2.getValue().fromX);
            }
        });
        return nextWindows.get(0).getKey();
    }

    private static EditorWindow findWindowAbove(final Position position, Map<EditorWindow, Position> windowPositions) {
        List<Map.Entry<EditorWindow, Position>> nextWindows = findAll(windowPositions.entrySet(), new Condition<Map.Entry<EditorWindow, Position>>() {
            @Override
            public boolean value(Map.Entry<EditorWindow, Position> entry) {
                return position.fromY == entry.getValue().toY;
            }
        });
        sort(nextWindows, new Comparator<Map.Entry<EditorWindow, Position>>() {
            @Override
            public int compare(Map.Entry<EditorWindow, Position> o1, Map.Entry<EditorWindow, Position> o2) {
                return Double.compare(o1.getValue().fromX, o2.getValue().fromX);
            }
        });
        return nextWindows.get(0).getKey();
    }

    private static EditorWindow findWindowLeftOf(final Position position, Map<EditorWindow, Position> windowPositions) {
        List<Map.Entry<EditorWindow, Position>> nextWindows = findAll(windowPositions.entrySet(), new Condition<Map.Entry<EditorWindow, Position>>() {
            @Override
            public boolean value(Map.Entry<EditorWindow, Position> entry) {
                return position.fromX == entry.getValue().toX;
            }
        });
        sort(nextWindows, new Comparator<Map.Entry<EditorWindow, Position>>() {
            @Override
            public int compare(Map.Entry<EditorWindow, Position> o1, Map.Entry<EditorWindow, Position> o2) {
                return Double.compare(o1.getValue().fromY, o2.getValue().fromY);
            }
        });
        return nextWindows.get(0).getKey();
    }
}
