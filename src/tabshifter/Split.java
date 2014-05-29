package tabshifter;

import static java.lang.Math.max;

public class Split extends LayoutElement {
    final LayoutElement first;
    final LayoutElement second;
    final boolean vertical;

    public Split(LayoutElement first, LayoutElement second, boolean vertical) {
        this.first = first;
        this.second = second;
        this.vertical = vertical;
    }

    @Override public Size size() {
        return vertical ?
            new Size(first.size().width + second.size().width, max(first.size().height, second.size().height)) :
            new Size(max(first.size().width, second.size().width), first.size().height + second.size().height);
    }

    @Override public String toString() {
        return "Split{" + (vertical ? "vertical, " : "") + first + ", " + second + "}";
    }
}
