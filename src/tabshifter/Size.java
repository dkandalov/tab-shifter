package tabshifter;

public class Size {
    public final int width;
    public final int height;

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override public String toString() {
        return "Size{width=" + width + ", height=" + height + "}";
    }
}
