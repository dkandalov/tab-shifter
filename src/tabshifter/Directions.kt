package tabshifter

import tabshifter.valueobjects.*
import kotlin.math.abs

object Directions {
    val left: Direction = object: Direction {
        override fun findTargetWindow(window: Window, layout: LayoutElement) = findWindowLeftOf(window, layout)
        override fun splitOrientation() = Split.Orientation.vertical
        override fun canExpand() = false
    }
    val up: Direction = object: Direction {
        override fun findTargetWindow(window: Window, layout: LayoutElement) = findWindowAbove(window, layout)
        override fun splitOrientation() = Split.Orientation.horizontal
        override fun canExpand() = false
    }
    val right: Direction = object: Direction {
        override fun findTargetWindow(window: Window, layout: LayoutElement) = findWindowRightOf(window, layout)
        override fun splitOrientation() = Split.Orientation.vertical
        override fun canExpand() = true

    }
    val down: Direction = object: Direction {
        override fun findTargetWindow(window: Window, layout: LayoutElement) = findWindowBelow(window, layout)
        override fun splitOrientation() = Split.Orientation.horizontal
        override fun canExpand() = true
    }

    private fun findWindowRightOf(window: Window, layout: LayoutElement): Window? {
        return layout.traverse().filterIsInstance<Window>()
            .filter { window.position.toX == it.position.fromX }
            .minBy { abs(window.position.fromY - it.position.fromY) }
    }

    private fun findWindowBelow(window: Window, layout: LayoutElement): Window? {
        return layout.traverse().filterIsInstance<Window>()
            .filter { window.position.toY == it.position.fromY }
            .minBy { abs(window.position.fromX - it.position.fromX) }
    }

    private fun findWindowAbove(window: Window, layout: LayoutElement): Window? {
        return layout.traverse().filterIsInstance<Window>()
            .filter { window.position.fromY == it.position.toY }
            .minBy { abs(window.position.fromX - it.position.fromX) }
    }

    private fun findWindowLeftOf(window: Window, layout: LayoutElement): Window? {
        return layout.traverse().filterIsInstance<Window>()
            .filter { window.position.fromX == it.position.toX }
            .minBy { abs(window.position.fromY - it.position.fromY) }
    }

    interface Direction {
        fun findTargetWindow(window: Window, layout: LayoutElement): Window?
        fun splitOrientation(): Split.Orientation
        fun canExpand(): Boolean
    }
}