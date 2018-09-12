package tabshifter.valueobjects;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

public class Split extends LayoutElement {
	/**
	 * Note that IntelliJ Splitter has reverse meaning of orientation.
	 */
	public enum Orientation {
		/**
		 * Most lines and editors are "vertical".
		 * ┌─┬─┐
		 * │ │ │
		 * └─┴─┘
		 */
		vertical,
		/**
		 * Most lines and editors are "horizontal".
		 * ┌───┐
		 * ├───┤
		 * └───┘
		 */
		horizontal
	}

	public final LayoutElement first;
	public final LayoutElement second;
	public final Orientation orientation;


	public Split(LayoutElement first, LayoutElement second, Orientation orientation) {
		this.first = first;
		this.second = second;
		this.orientation = orientation;
	}

	@Override public Size size() {
		if (orientation == Orientation.vertical) {
			return new Size(
				first.size().width + second.size().width,
				max(first.size().height, second.size().height)
			);
		} else {
			return new Size(
				max(first.size().width, second.size().width),
				first.size().height + second.size().height
			);
		}
	}

	@Override public String toString() {
		return "Split(" + orientation + " " + first + ", " + second + ")";
	}

	public static List<Split> allSplitsIn(LayoutElement rootElement) {
		final List<Split> result = new ArrayList<>();
		traverse(rootElement, element -> {
			if (element instanceof Split) {
				result.add((Split) element);
			}
			return true;
		});
		return result;
	}
}
