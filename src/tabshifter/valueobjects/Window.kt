package tabshifter.valueobjects

open class Window(
    val hasOneTab: Boolean,
    val isCurrent: Boolean,
    val pinnedFiles: List<String>
): LayoutElement() {
    override val size = Size(1, 1)

    override fun toString() = "Window-" + Integer.toHexString(hashCode())
}
