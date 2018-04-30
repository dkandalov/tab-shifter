package tabshifter;

import org.jetbrains.annotations.Nullable;
import tabshifter.valueobjects.LayoutElement;
import tabshifter.valueobjects.Split;
import tabshifter.valueobjects.Window;

import java.util.List;

import static com.intellij.util.containers.ContainerUtil.findAll;
import static com.intellij.util.containers.ContainerUtil.sort;
import static java.util.Comparator.comparingDouble;
import static tabshifter.valueobjects.Split.Orientation.horizontal;
import static tabshifter.valueobjects.Split.Orientation.vertical;
import static tabshifter.valueobjects.Window.allWindowsIn;

public class Directions {
	public static final Direction left = new Direction() {
		@Override @Nullable public Window findTargetWindow(Window window, LayoutElement layout) {
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
		@Override @Nullable public Window findTargetWindow(Window window, LayoutElement layout) {
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
		@Override @Nullable public Window findTargetWindow(Window window, LayoutElement layout) {
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
		@Override @Nullable public Window findTargetWindow(Window window, LayoutElement layout) {
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
		List<Window> neighbourWindows = findAll(allWindows, it -> window.position.toX == it.position.fromX);
		sort(neighbourWindows, comparingDouble(it -> Math.abs(window.position.fromY - it.position.fromY)));
		return neighbourWindows.isEmpty() ? null : neighbourWindows.get(0);
	}

	private static Window findWindowBelow(final Window window, LayoutElement layout) {
		List<Window> allWindows = allWindowsIn(layout);
		List<Window> neighbourWindows = findAll(allWindows, it -> window.position.toY == it.position.fromY);
		sort(neighbourWindows, comparingDouble(it -> Math.abs(window.position.fromX - it.position.fromX)));
		return neighbourWindows.isEmpty() ? null : neighbourWindows.get(0);
	}

	private static Window findWindowAbove(final Window window, LayoutElement layout) {
		List<Window> allWindows = allWindowsIn(layout);
		List<Window> neighbourWindows = findAll(allWindows, it -> window.position.fromY == it.position.toY);
		sort(neighbourWindows, comparingDouble(it -> Math.abs(window.position.fromX - it.position.fromX)));
		return neighbourWindows.isEmpty() ? null : neighbourWindows.get(0);
	}

	private static Window findWindowLeftOf(final Window window, LayoutElement layout) {
		List<Window> allWindows = allWindowsIn(layout);
		List<Window> neighbourWindows = findAll(allWindows, it -> window.position.fromX == it.position.toX);
		sort(neighbourWindows, comparingDouble(it -> Math.abs(window.position.fromY - it.position.fromY)));
		return neighbourWindows.isEmpty() ? null : neighbourWindows.get(0);
	}


	public interface Direction {
		@Nullable Window findTargetWindow(Window window, LayoutElement layout);

		Split.Orientation splitOrientation();

		boolean canExpand();
	}
}
