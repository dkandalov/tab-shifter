package tabshifter.valueobjects;

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
        return "(" + fromX + "->" + toX + ", " + fromY + "->" + toY + ")";
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (fromX != position.fromX) return false;
        if (fromY != position.fromY) return false;
        if (toX != position.toX) return false;
        if (toY != position.toY) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = fromX;
        result = 31 * result + fromY;
        result = 31 * result + toX;
        result = 31 * result + toY;
        return result;
    }
}
