package tabshifter;

import com.intellij.openapi.util.Condition;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import tabshifter.valueobjects.LayoutElement;
import tabshifter.valueobjects.Split;
import tabshifter.valueobjects.Window;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.intellij.util.containers.ContainerUtil.findAll;
import static com.intellij.util.containers.ContainerUtil.sort;
import static tabshifter.valueobjects.Split.Orientation.horizontal;
import static tabshifter.valueobjects.Split.Orientation.vertical;

public class Directions {
	public static final Direction left = new Direction() {
		@Override public Window findTargetWindow(Window window, LayoutElement layout) {
			return findWindowLeftOf(window, layout);
		}

		@Override public Split.Orientation splitOrientation() {
			return vertical;
		}

		@Override public boolean canExpand() {
			return false;
		}
	};
	public static final Direction up = new Direction() {
		@Override public Window findTargetWindow(Window window, LayoutElement layout) {
			return findWindowAbove(window, layout);
		}

		@Override public Split.Orientation splitOrientation() {
			return horizontal;
		}

		@Override public boolean canExpand() {
			return false;
		}
	};
	public static final Direction right = new Direction() {
		@Override public Window findTargetWindow(Window window, LayoutElement layout) {
			return findWindowRightOf(window, layout);
		}

		@Override public Split.Orientation splitOrientation() {
			return vertical;
		}

		@Override public boolean canExpand() {
			return true;
		}
	};
	public static final Direction down = new Direction() {
        @Override public Window findTargetWindow(Window window, LayoutElement layout) {
            return findWindowBelow(window, layout);
        }

        @Override public Split.Orientation splitOrientation() {
            return horizontal;
        }

        @Override public boolean canExpand() {
            return true;
        }
    };

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

	static List<Window> allWindowsIn(LayoutElement rootElement) {
        final List<Window> result = new ArrayList<Window>();
        traverse(rootElement, new Function<LayoutElement, Boolean>() {
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

	public interface Direction {
		Window findTargetWindow(Window window, LayoutElement layout);

		Split.Orientation splitOrientation();

		boolean canExpand();
	}
}
