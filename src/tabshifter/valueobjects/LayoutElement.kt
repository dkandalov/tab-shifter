package tabshifter.valueobjects

abstract class LayoutElement {
    var position: Position = Position(0, 0, 0, 0)
    abstract fun size(): Size

    companion object {
        val none = object: LayoutElement() {
            override fun size() = Size(0, 0)
            override fun toString() = "LayoutElement{None}"
        }
    }
}

fun LayoutElement?.traverse(): Sequence<LayoutElement> = sequence {
    if (this@traverse != null) {
        yield(this@traverse!!)
        if (this@traverse is Split) {
            yieldAll(first.traverse())
            yieldAll(second.traverse())
        }
    }
}

data class Position(val fromX: Int, val fromY: Int, val toX: Int, val toY: Int) {
    fun withFromX(value: Int) = Position(value, fromY, toX, toY)
    fun withFromY(value: Int) = Position(fromX, value, toX, toY)
    fun withToX(value: Int) = Position(fromX, fromY, value, toY)
    fun withToY(value: Int) = Position(fromX, fromY, toX, value)

    override fun toString() = "($fromX->$toX, $fromY->$toY)"
}

data class Size(val width: Int, val height: Int) {
    override fun toString() = "Size{width=$width, height=$height}"
}