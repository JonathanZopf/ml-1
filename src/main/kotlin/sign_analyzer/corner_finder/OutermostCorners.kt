package org.hszg.sign_analyzer.corner_finder

import org.opencv.core.Point


class OutermostCorners(
    private val top: Point,
    private val bottom: Point,
    private val left: Point,
    private val right: Point
) {
    fun getVerticalLine(): Pair<Point, Point> {
        return Pair(top, bottom)
    }

    fun getHorizontalLine(): Pair<Point, Point> {
        return Pair(left, right)
    }
}