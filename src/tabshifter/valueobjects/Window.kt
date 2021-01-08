package tabshifter.valueobjects

open class Window(val hasOneTab: Boolean, val isCurrent: Boolean): LayoutElement() {
    override val size = Size(1, 1)

    override fun toString() = "Window-" + Integer.toHexString(hashCode())
}
