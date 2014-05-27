package tabshifter;

public class Position {
    public final int fromX;
    public final int fromY;
    public final int toX;
    public final int toY;

    public Position(int fromX, int fromY, int toX, int toY) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }

    public Position withFromX(int value) {
        return new Position(value, fromY, toX, toY);
    }

    public Position withFromY(int value) {
        return new Position(fromX, value, toX, toY);
    }

    public Position withToX(int value) {
        return new Position(fromX, fromY, value, toY);
    }

    public Position withToY(int value) {
        return new Position(fromX, fromY, toX, value);
    }

    @Override public String toString() {
        return "(" + fromX + "," + fromY + ")->(" + toX + "," + toY + ")";
    }
}
