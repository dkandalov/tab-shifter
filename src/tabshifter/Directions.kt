package tabshifter

import tabshifter.layout.*
import kotlin.math.abs

enum class Direction(
    val splitOrientation: Split.Orientation,
    val findTargetWindow: (window: Window, layout: LayoutElement) -> Window?
) {
    left(Split.Orientation.vertical, ::findWindowLeftOf),
    up(Split.Orientation.horizontal, ::findWindowAbove),
    right(Split.Orientation.vertical, ::findWindowRightOf),
    down(Split.Orientation.horizontal, ::findWindowBelow),
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
