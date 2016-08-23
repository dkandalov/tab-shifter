package tabshifter.valueobjects;

import com.intellij.util.Function;

public abstract class LayoutElement {

	public Position position;

	public abstract Size size();


	public static final LayoutElement none = new LayoutElement() {
		@Override public Size size() {
			return new Size(0, 0);
		}

		@Override public String toString() {
			return "LayoutElement{None}";
		}
	};

	public static void traverse(LayoutElement element, Function<LayoutElement, Boolean> function) {
		Boolean shouldStop = !function.fun(element);
		if (shouldStop) return;

		if (element instanceof Split) {
			Split split = (Split) element;
			traverse(split.first, function);
			traverse(split.second, function);
		}
	}
}
