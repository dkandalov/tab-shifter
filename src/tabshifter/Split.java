package tabshifter;

import static java.lang.Math.max;

public class Split extends LayoutElement {
    public enum Orientation {
        vertical, horizontal
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
        return orientation == Orientation.vertical ?
            new Size(first.size().width + second.size().width, max(first.size().height, second.size().height)) :
            new Size(max(first.size().width, second.size().width), first.size().height + second.size().height);
    }

    @Override public String toString() {
        return "Split{" + orientation + " " + first + ", " + second + "}";
    }
}
