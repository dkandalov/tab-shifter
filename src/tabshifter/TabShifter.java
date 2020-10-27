package tabshifter;

import com.intellij.openapi.diagnostic.*;
import org.jetbrains.annotations.*;
import tabshifter.Directions.*;
import tabshifter.valueobjects.*;

import static com.intellij.util.containers.ContainerUtil.*;
import static tabshifter.Directions.*;
import static tabshifter.valueobjects.Split.Orientation.*;
import static tabshifter.valueobjects.Split.*;
import static tabshifter.valueobjects.Window.*;

public class TabShifter {
	public static final TabShifter none = new TabShifter(null) {
		@Override public void moveFocus(Direction direction) { }
		@Override public void moveTab(Direction direction) { }
		@Override public void stretchSplitter(Direction direction) { }
	};
	private static final Logger logger = Logger.getInstance(TabShifter.class.getName());

	private final Ide ide;


	public TabShifter(Ide ide) {
		this.ide = ide;
	}

	/**
	 * Potentially this mysterious component com.intellij.ui.switcher.SwitchManagerAppComponent
	 * could be used for switching focus, but it's currently doesn't work very well and is not enabled.
	 */
	public void moveFocus(Direction direction) {
		LayoutElement layout = calculateAndSetPositions(ide.snapshotWindowLayout());
		if (layout == LayoutElement.none) return;

		Window window = currentWindowIn(layout);
		if (window == null) return;

		Window targetWindow = direction.findTargetWindow(window, layout);
		if (targetWindow == null) return;

		ide.setFocusOn(targetWindow);
	}

	/**
	 * Moves tab in the specified direction.
     *
	 * This is way more complicated than it should have been. The main reasons are:
	 * - closing/opening or opening/closing tab doesn't guarantee that focus will be in the moved tab,
	 *   therefore, need to track target window to move focus into it
	 * - EditorWindow object changes its identity after split/unsplit (i.e. points to another visual window)
	 *   therefore, need to predict target window position and look up window by expected position
	 */
	public void moveTab(Direction direction) {
		LayoutElement layout = calculateAndSetPositions(ide.snapshotWindowLayout());
		if (layout == LayoutElement.none) return;
		Window window = currentWindowIn(layout);
		if (window == null) return;

		Window targetWindow = direction.findTargetWindow(window, layout);

		Position newPosition;

		boolean isAtEdge = (targetWindow == null);
		if (isAtEdge) {
			if (window.hasOneTab || !direction.canExpand()) return;

			LayoutElement newLayout = insertSplit(direction.splitOrientation(), window, layout);
			calculateAndSetPositions(newLayout);
			LayoutElement sibling = findSiblingOf(window, newLayout);
			if (sibling == null) return; // should never happen
			newPosition = sibling.position;

			ide.createSplitter(direction.splitOrientation());
		} else {
			boolean willBeUnsplit = window.hasOneTab;
			if (willBeUnsplit) {
				LayoutElement unsplitLayout = removeFrom(layout, window);
				calculateAndSetPositions(unsplitLayout);
			}
			newPosition = targetWindow.position;

			ide.openCurrentFileIn(targetWindow);
		}
		ide.closeCurrentFileIn(window, () -> {
			LayoutElement newWindowLayout = calculateAndSetPositions(ide.snapshotWindowLayout());
			// Do this because identity of the window object can change after closing the current file.
			Window targetWindowLookedUpAgain = findWindowBy(newPosition, newWindowLayout);

			if (targetWindowLookedUpAgain == null) {
				// ideally this should never happen, logging in case something goes wrong
				logger.warn("No window for: " + newPosition + "; windowLayout: " + newWindowLayout);
			} else {
				ide.setFocusOn(targetWindowLookedUpAgain);
			}
		});
	}

	public void stretchSplitter(Direction direction) {
		LayoutElement layout = calculateAndSetPositions(ide.snapshotWindowLayout());
		if (layout == LayoutElement.none) return;

		Window window = currentWindowIn(layout);
		if (window == null) return;

		Split split = findParentSplitOf(window, layout);
		Split.Orientation orientationToSkip = (direction == left || direction == right) ? horizontal : vertical;
		while (split != null && split.orientation == orientationToSkip) {
			split = findParentSplitOf(split, layout);
		}
		if (split == null) return;

		if (direction == right || direction == down) {
			ide.growSplitProportion(split);
		} else {
			ide.shrinkSplitProportion(split);
		}
	}

	public void toggleMaximizeRestoreSplitter() {
		LayoutElement layout = calculateAndSetPositions(ide.snapshotWindowLayout());
		if (layout == LayoutElement.none) return;

		Window window = currentWindowIn(layout);
		Split split = findParentSplitOf(window, layout);
		if (split == null) return;
		boolean inFirst = split.first.equals(window);

		boolean maximized = ide.toggleMaximizeRestoreSplitter(split, inFirst);
		if (maximized) {
			ide.hideToolWindows();
		}
	}

	public void equalSizeSplitter() {
		LayoutElement layout = calculateAndSetPositions(ide.snapshotWindowLayout());
		if (layout == LayoutElement.none) return;

		Split split = findParentSplitOf(currentWindowIn(layout), layout);
		if (split == null) return;

		ide.equalSizeSplitter(split);
	}

	@Nullable private static Split findParentSplitOf(LayoutElement layoutElement, LayoutElement layout) {
		for (Split split : allSplitsIn(layout)) {
			if (split.first.equals(layoutElement) || split.second.equals(layoutElement)) {
				return split;
			}
		}
		return null;
	}

	@Nullable private static Window currentWindowIn(LayoutElement windowLayout) {
		return find(allWindowsIn(windowLayout), window -> window.isCurrent);
	}

	private static LayoutElement findSiblingOf(Window window, LayoutElement element) {
		if (element instanceof Split) {
			Split split = (Split) element;

			if (split.first.equals(window)) return split.second;
			if (split.second.equals(window)) return split.first;

			LayoutElement first = findSiblingOf(window, split.first);
			if (first != null) return first;

			return findSiblingOf(window, split.second);

		} else if (element instanceof Window) {
			return null;

		} else {
			throw new IllegalStateException();
		}
	}

	private static LayoutElement calculateAndSetPositions(LayoutElement element) {
		return calculateAndSetPositions(element, new Position(0, 0, element.size().width, element.size().height));
	}

	private static LayoutElement calculateAndSetPositions(LayoutElement element, Position position) {
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
			calculateAndSetPositions(split.first, firstPosition);
			calculateAndSetPositions(split.second, secondPosition);
		}

		element.position = position;
		return element;
	}

	@Nullable private static Window findWindowBy(final Position position, LayoutElement layout) {
		return find(allWindowsIn(layout), window -> position.equals(window.position));
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

	private static LayoutElement insertSplit(Split.Orientation orientation, Window window, LayoutElement element) {
		if (element instanceof Split) {
			Split split = (Split) element;
			return new Split(
					insertSplit(orientation, window, split.first),
					insertSplit(orientation, window, split.second),
					split.orientation
			);
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
}
