package tabshifter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Condition;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.intellij.util.containers.ContainerUtil.*;
import static tabshifter.Split.Orientation.horizontal;
import static tabshifter.Split.Orientation.vertical;

public class TabShifter {
    private static final Logger logger = Logger.getInstance(TabShifter.class.getName());
    private final Ide ide;

    public TabShifter(Ide ide) {
        this.ide = ide;
    }

    public void moveTabLeft() {
        moveTab(new LeftDirection());
    }

    public void moveTabUp() {
        moveTab(new UpDirection());
    }

    public void moveTabRight() {
        moveTab(new RightDirection());
    }

    public void moveTabDown() {
        moveTab(new DownDirection());
    }

    private void moveTab(MovingDirection direction) {
        LayoutElement layout = calculatePositions(ide.snapshotWindowLayout());
        Window window = currentWindowIn(layout);
        Window targetWindow = direction.targetWindow(window, layout);

        Position newPosition;

        boolean isAtEdge = (targetWindow == null);
        if (isAtEdge) {
            if (window.hasOneTab || !direction.canExpand()) return;

            LayoutElement newLayout = insertSplit(direction.splitOrientation(), window, layout);
            calculatePositions(newLayout);
            newPosition = findSiblingOf(window, newLayout).position;

            ide.createSplitter(direction.splitOrientation());
            ide.closeCurrentFileIn(window);

        } else {
            boolean willBeUnsplit = window.hasOneTab;
            if (willBeUnsplit) {
                LayoutElement unsplitLayout = removeFrom(layout, window);
                calculatePositions(unsplitLayout);
            }
            newPosition = targetWindow.position;

            ide.openCurrentFileIn(targetWindow);
            ide.closeCurrentFileIn(window);
        }

        LayoutElement newWindowLayout = calculatePositions(ide.snapshotWindowLayout());
        targetWindow = findWindowBy(newPosition, allWindowsIn(newWindowLayout));

        if (targetWindow == null) {
            // ideally this should never happen, logging in case something goes wrong
            logger.warn("No window for: " + newPosition);
        } else {
            ide.setFocusOn(targetWindow);
        }
    }

    private static interface MovingDirection {
        Window targetWindow(Window window, LayoutElement layout);
        Split.Orientation splitOrientation();
        boolean canExpand();
    }

    private static class DownDirection implements MovingDirection {
        @Override public Window targetWindow(Window window, LayoutElement layout) {
            return findWindowBelow(window, layout);
        }

        @Override public Split.Orientation splitOrientation() {
            return horizontal;
        }

        @Override public boolean canExpand() {
            return true;
        }
    }

    private static class RightDirection implements MovingDirection {
        @Override public Window targetWindow(Window window, LayoutElement layout) {
            return findWindowRightOf(window, layout);
        }

        @Override public Split.Orientation splitOrientation() {
            return vertical;
        }

        @Override public boolean canExpand() {
            return true;
        }
    }

    private static class UpDirection implements MovingDirection {
        @Override public Window targetWindow(Window window, LayoutElement layout) {
            return findWindowAbove(window, layout);
        }

        @Override public Split.Orientation splitOrientation() {
            return horizontal;
        }

        @Override public boolean canExpand() {
            return false;
        }
    }

    private static class LeftDirection implements MovingDirection {
        @Override public Window targetWindow(Window window, LayoutElement layout) {
            return findWindowLeftOf(window, layout);
        }

        @Override public Split.Orientation splitOrientation() {
            return vertical;
        }

        @Override public boolean canExpand() {
            return false;
        }
    }

    private static LayoutElement findSiblingOf(Window window, LayoutElement element) {
        if (element instanceof Split) {
            Split split = (Split) element;

            if (split.first.equals(window)) return split.second;
            if (split.second.equals(window)) return split.first;

            LayoutElement first = findSiblingOf(window, split.first);
            if (first != null) return first;
            LayoutElement second = findSiblingOf(window, split.second);
            if (second != null) return second;

            return null;

        } else if (element instanceof Window) {
            return null;

        } else {
            throw new IllegalStateException();
        }
    }

    private static LayoutElement insertSplit(Split.Orientation orientation, Window window, LayoutElement element) {
        if (element instanceof Split) {
            Split split = (Split) element;
            return new Split(
                    insertSplit(orientation, window, split.first),
                    insertSplit(orientation, window, split.second),
                    split.orientation);
        } else if (element instanceof Window) {
            if (element.equals(window)) {
                return new Split(window, new Window(true, false), orientation);
            } else {
                return element;
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private static Window currentWindowIn(LayoutElement windowLayout) {
        return find(allWindowsIn(windowLayout), new Condition<Window>() {
            @Override
            public boolean value(Window window) {
                return window.isCurrent;
            }
        });
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

    private static Window findWindowBelow(final Window window, LayoutElement layout) {
        List<Window> allWindows = allWindowsIn(layout);
        List<Window> neighbourWindows = findAll(allWindows, new Condition<Window>() {
            @Override
            public boolean value(Window window1) {
                return window.position.toY == window1.position.fromY;
            }
        });
        sort(neighbourWindows, new Comparator<Window>() {
            @Override
            public int compare(@NotNull Window o1, @NotNull Window o2) {
                return Double.compare(
                        Math.abs(window.position.fromX - o1.position.fromX),
                        Math.abs(window.position.fromX - o2.position.fromX)
                );
            }
        });
        return neighbourWindows.isEmpty() ? null : neighbourWindows.get(0);
    }

    private static Window findWindowAbove(final Window window, LayoutElement layout) {
        List<Window> allWindows = allWindowsIn(layout);
        List<Window> neighbourWindows = findAll(allWindows, new Condition<Window>() {
            @Override
            public boolean value(Window window1) {
                return window.position.fromY == window1.position.toY;
            }
        });
        sort(neighbourWindows, new Comparator<Window>() {
            @Override
            public int compare(@NotNull Window o1, @NotNull Window o2) {
                return Double.compare(
                        Math.abs(window.position.fromX - o1.position.fromX),
                        Math.abs(window.position.fromX - o2.position.fromX)
                );
            }
        });
        return neighbourWindows.isEmpty() ? null : neighbourWindows.get(0);
    }

    private static Window findWindowLeftOf(final Window window, LayoutElement layout) {
        List<Window> allWindows = allWindowsIn(layout);
        List<Window> neighbourWindows = findAll(allWindows, new Condition<Window>() {
            @Override
            public boolean value(Window window1) {
                return window.position.fromX == window1.position.toX;
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

    private static LayoutElement calculatePositions(LayoutElement element) {
        return calculatePositions(element, new Position(0, 0, element.size().width, element.size().height));
    }

    private static LayoutElement calculatePositions(LayoutElement element, Position position) {
        if (element instanceof Split) {
            Split split = (Split) element;

            Position firstPosition;
            Position secondPosition;
            if (split.orientation == vertical) {
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
            else return new Split(first, second, split.orientation);

        } else if (element instanceof Window) {
            return element.equals(window) ? null : element;

        } else {
            throw new IllegalStateException();
        }
    }
}
