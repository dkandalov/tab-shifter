package tabshifter;

import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.util.Condition;
import com.intellij.util.Function;
import liveplugin.PluginUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.intellij.util.containers.ContainerUtil.*;

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

        EditorWindow leftWindow = findWindowLeftOf(position, windowPositions);
        Position leftWindowPosition = windowPositions.get(leftWindow);
        boolean isTheOnlyTab = (window.getTabCount() == 1);
        boolean inTheSameSplit = ide.inTheSameSplit(window, leftWindow);
        Position mergedPosition = isTheOnlyTab && inTheSameSplit ? leftWindowPosition.merge(position) : leftWindowPosition;

        Size size = ide.layoutSize();

        ide.reopenFileIn(leftWindow);
        ide.closeCurrentFileIn(window);

        Size newSize = ide.layoutSize();
        final Position newMergedPosition = adjustedPosition(size, newSize, mergedPosition);
        EditorWindow newWindow = findWindow(newMergedPosition);
        ide.setFocusOn(newWindow);
    }

    public void moveTabUp() {
        EditorWindow window = ide.currentWindow();
        Map<EditorWindow, Position> windowPositions = ide.windowPositions();
        Position position = windowPositions.get(window);

        boolean isTopWindow = (position.fromY == 0);
        if (isTopWindow) return;

        EditorWindow windowAbove = findWindowAbove(position, windowPositions);
        Position windowAbovePosition = windowPositions.get(windowAbove);
        boolean isTheOnlyTab = (window.getTabCount() == 1);
        boolean inTheSameSplit = ide.inTheSameSplit(window, windowAbove);
        Position mergedPosition = isTheOnlyTab && inTheSameSplit ? windowAbovePosition.merge(position) : windowAbovePosition;

        Size size = ide.layoutSize();

        ide.reopenFileIn(windowAbove);
        ide.closeCurrentFileIn(window);

        Size newSize = ide.layoutSize();
        Position newMergedPosition = adjustedPosition(size, newSize, mergedPosition);
        ide.setFocusOn(findWindow(newMergedPosition));
    }

    public void moveTabRight() { // TODO do the same for tab moves in other directions
        LayoutElement layout = calculatePositions(ide.snapshotWindowLayout());
        Window window = currentWindowIn(layout);
        Window targetWindow = findWindowRightOf(window, layout);

        Position newPosition;

        boolean isAtEdge = (targetWindow == null);
        if (isAtEdge) {
            if (window.hasOneTab) return;

            newPosition = window.position
                    .withFromX(window.position.fromX + 1)
                    .withToX(window.position.toX + 1);

            ide.createSplitter(SwingConstants.VERTICAL);
            ide.closeCurrentFileIn(window);

        } else {
            boolean willBeUnsplit = window.hasOneTab;
            if (willBeUnsplit) {
                LayoutElement unsplitLayout = removeFrom(layout, window);
                calculatePositions(unsplitLayout);
            }
            newPosition = targetWindow.position;

            ide.reopenFileIn(targetWindow);
            ide.closeCurrentFileIn(window);
        }

        LayoutElement newWindowLayout = calculatePositions(ide.snapshotWindowLayout());
        ide.setFocusOn(findWindowBy(newPosition, allWindowsIn(newWindowLayout)));
    }

    private static Window currentWindowIn(LayoutElement windowLayout) {
        return find(allWindowsIn(windowLayout), new Condition<Window>() {
            @Override
            public boolean value(Window window) {
                return window.isCurrent;
            }
        });
    }

    public void moveTabDown() {
        EditorWindow window = ide.currentWindow();
        Map<EditorWindow, Position> windowPositions = ide.windowPositions();
        Position position = windowPositions.get(window);

        boolean isBottomWindow = (position.toY == ide.layoutSize().height);
        if (isBottomWindow && ide.currentSplitTabCount() == 1) return;

        if (isBottomWindow) {
            EditorWindow newWindow = ide.createSplitter(SwingConstants.HORIZONTAL);
            ide.closeCurrentFileIn(window);
            ide.setFocusOn(newWindow);
        } else {
            EditorWindow windowBelow = findWindowBelow(position, windowPositions);
            Position windowBelowPosition = windowPositions.get(windowBelow);

            boolean isTheOnlyTab = (window.getTabCount() == 1);
            boolean inTheSameSplit = ide.inTheSameSplit(window, windowBelow);
            Position mergedPosition = isTheOnlyTab && inTheSameSplit ? windowBelowPosition.merge(position) : windowBelowPosition;

            Size size = ide.layoutSize();

            ide.reopenFileIn(windowBelow);
            ide.closeCurrentFileIn(window);

            Size newSize = ide.layoutSize();
            Position newMergedPosition = adjustedPosition(size, newSize, mergedPosition);
            ide.setFocusOn(findWindow(newMergedPosition));
        }
    }

    @SuppressWarnings("ConstantConditions")
    private EditorWindow findWindow(final Position newMergedPosition) {
        Map.Entry<EditorWindow, Position> entry = find(ide.windowPositions().entrySet(), new Condition<Map.Entry<EditorWindow, Position>>() {
            @Override
            public boolean value(Map.Entry<EditorWindow, Position> entry) {
                return entry.getValue().equals(newMergedPosition);
            }
        });
        if (entry == null) {
            PluginUtil.show(newMergedPosition);
            throw new IllegalStateException();
        }
        return entry.getKey();
    }

    private Position adjustedPosition(Size size, Size newSize, Position mergedPosition) {
        final Position newMergedPosition;
        if (!size.equals(newSize)) {
            if (newSize.width < size.width) {
                newMergedPosition = mergedPosition.withToX(mergedPosition.toX - 1);
            } else if (newSize.height < size.height) {
                newMergedPosition = mergedPosition.withToY(mergedPosition.toY - 1);
            } else {
                throw new IllegalStateException();
            }
        } else {
            newMergedPosition = mergedPosition;
        }
        return newMergedPosition;
    }

    private static Window findWindowRightOf(final Window window, LayoutElement layout) {
        List<Window> allWindows = allWindowsIn(layout);
        List<Window> neighbourWindows = findAll(allWindows, new Condition<Window>() {
            @Override
            public boolean value(Window window1) {
                return window.position.toX == window1.position.fromX;
            }
        });
        sort(neighbourWindows, new Comparator<Window>() {
            @Override
            public int compare(@NotNull Window o1, @NotNull Window o2) {
                return Double.compare(
                        Math.abs(window.position.fromY - o1.position.fromY),
                        Math.abs(window.position.fromY - o2.position.fromY)
                );
            }
        });
        return neighbourWindows.isEmpty() ? null : neighbourWindows.get(0);
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
                return Double.compare(
                        Math.abs(position.fromX - o1.getValue().fromX),
                        Math.abs(position.fromX - o2.getValue().fromX)
                );
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
                return Double.compare(
                        Math.abs(position.fromX - o1.getValue().fromX),
                        Math.abs(position.fromX - o2.getValue().fromX)
                );
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
                return Double.compare(
                        Math.abs(position.fromY - o1.getValue().fromY),
                        Math.abs(position.fromY - o2.getValue().fromY)
                );
            }
        });
        return nextWindows.get(0).getKey();
    }


    private static LayoutElement calculatePositions(LayoutElement element) {
        return calculatePositions(element, new Position(0, 0, element.size().width, element.size().height));
    }

    private static LayoutElement calculatePositions(LayoutElement element, Position position) {
        if (element instanceof Split) {
            Split split = (Split) element;

            Position firstPosition;
            Position secondPosition;
            if (split.vertical) {
                firstPosition = position.withToX(position.toX - split.second.size().width);
                secondPosition = position.withFromX(position.fromX + split.first.size().width);
            } else {
                firstPosition = position.withToY(position.toY - split.second.size().height);
                secondPosition = position.withFromY(position.fromY + split.first.size().height);
            }
            calculatePositions(split.first, firstPosition);
            calculatePositions(split.second, secondPosition);
        }

        element.position = position;
        return element;
    }

    private static Window findWindowBy(final Position position, List<Window> windows) {
        Window window = find(windows, new Condition<Window>() {
            @Override
            public boolean value(Window window) {
                return position.equals(window.position);
            }
        });
        if (window == null) {
            PluginUtil.show("No window for: " + position);
        }
        return window;
    }

    private static List<Window> allWindowsIn(LayoutElement rootElement) {
        final List<Window> result = new ArrayList<Window>();
        TabShifter.traverse(rootElement, new Function<LayoutElement, Boolean>() {
            @Override
            public Boolean fun(LayoutElement element) {
                if (element instanceof Window)
                    result.add((Window) element);
                return true;
            }
        });
        return result;
    }

    private static void traverse(LayoutElement element, Function<LayoutElement, Boolean> function) {
        Boolean shouldStop = !function.fun(element);
        if (shouldStop) return;

        if (element instanceof Split) {
            Split split = (Split) element;
            traverse(split.first, function);
            traverse(split.second, function);
        }
    }

    private static LayoutElement removeFrom(LayoutElement element, Window window) {
        if (element instanceof Split) {
            Split split = (Split) element;
            LayoutElement first = removeFrom(split.first, window);
            LayoutElement second = removeFrom(split.second, window);

            if (first == null) return second;
            else if (second == null) return first;
            else return new Split(first, second, split.vertical);

        } else if (element instanceof Window) {
            return element.equals(window) ? null : element;

        } else {
            throw new IllegalStateException();
        }
    }
}
