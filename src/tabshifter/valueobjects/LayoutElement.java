package tabshifter.valueobjects;

public abstract class LayoutElement {
    public static final LayoutElement none = new LayoutElement() {
        @Override public Size size() {
            return new Size(0, 0);
        }

        @Override public String toString() {
            return "LayoutElement{None}";
        }
    };

    public Position position;

    public abstract Size size();
}
