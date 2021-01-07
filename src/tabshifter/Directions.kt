package tabshifter

import tabshifter.valueobjects.*
import kotlin.math.abs

object Directions {
    val left = object: Direction {
        override fun findTargetWindow(window: Window, layout: LayoutElement) = findWindowLeftOf(window, layout)
        override val splitOrientation = Split.Orientation.vertical
        override val canExpand = false
    }
    val up = object: Direction {
        override fun findTargetWindow(window: Window, layout: LayoutElement) = findWindowAbove(window, layout)
        override val splitOrientation = Split.Orientation.horizontal
        override val canExpand = false
    }
    val right = object: Direction {
        override fun findTargetWindow(window: Window, layout: LayoutElement) = findWindowRightOf(window, layout)
        override val splitOrientation = Split.Orientation.vertical
        override val canExpand = true

    }
    val down = object: Direction {
        override fun findTargetWindow(window: Window, layout: LayoutElement) = findWindowBelow(window, layout)
        override val splitOrientation = Split.Orientation.horizontal
        override val canExpand = true
    }

    private fun findWindowRightOf(window: Window, layout: LayoutElement): Window? =
        layout.traverse().filterIsInstance<Window>()
            .filter { window.position.toX == it.position.fromX }
            .minBy { abs(window.position.fromY - it.position.fromY) }

    private fun findWindowBelow(window: Window, layout: LayoutElement): Window? =
        layout.traverse().filterIsInstance<Window>()
            .filter { window.position.toY == it.position.fromY }
            .minBy { abs(window.position.fromX - it.position.fromX) }

    private fun findWindowAbove(window: Window, layout: LayoutElement): Window? =
        layout.traverse().filterIsInstance<Window>()
            .filter { window.position.fromY == it.position.toY }
            .minBy { abs(window.position.fromX - it.position.fromX) }

    private fun findWindowLeftOf(window: Window, layout: LayoutElement): Window? =
        layout.traverse().filterIsInstance<Window>()
            .filter { window.position.fromX == it.position.toX }
            .minBy { abs(window.position.fromY - it.position.fromY) }

    interface Direction {
        fun findTargetWindow(window: Window, layout: LayoutElement): Window?
        val splitOrientation: Split.Orientation
        val canExpand: Boolean
    }
}