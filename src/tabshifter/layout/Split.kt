package tabshifter.layout

import kotlin.math.max

open class Split(
    val first: LayoutElement,
    val second: LayoutElement,
    val orientation: Orientation
): LayoutElement() {

    /**
     * Note that IntelliJ Splitter has reverse meaning of orientation.
     */
    enum class Orientation {
        /**
         * Most lines and editors are "vertical".
         * ┌─┬─┐
         * │ │ │
         * └─┴─┘
         */
        vertical,

        /**
         * Most lines and editors are "horizontal".
         * ┌───┐
         * ├───┤
         * └───┘
         */
        horizontal
    }

    override val size =
        if (orientation == Orientation.vertical) Size(
            width = first.size.width + second.size.width,
            height = max(first.size.height, second.size.height)
        ) else Size(
            width = max(first.size.width, second.size.width),
            height = first.size.height + second.size.height
        )

    override fun toString() =
        "Split($orientation $first, $second)"
}