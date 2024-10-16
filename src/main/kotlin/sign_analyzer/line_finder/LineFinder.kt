package org.hszg.sign_analyzer.line_finder

import org.hszg.sign_analyzer.shape_recognizer.SignShape
import org.hszg.sign_analyzer.shape_recognizer.recognizeShape
import org.opencv.core.MatOfPoint
import org.opencv.core.Point

/**
 * Finds the horizontal line of a sign
 * Can be used for color analysis
 * @param extremities the extremities of the sign
 * @return the left and right point of the sign for creating a line
 */
fun findHorizontalLine(extremities: MatOfPoint): Pair<Point, Point>  {
    return findLine(extremities, LineFinderDirection.HORIZONTAL)
}

/**
 * Finds the vertical line of a sign
 * Can be used for color analysis
 * @param extremities the extremities of the sign
 * @return the top and bottom point of the sign for creating a line
 */
fun findVerticalLine(extremities: MatOfPoint) : Pair<Point, Point>{
    return findLine(extremities, LineFinderDirection.VERTICAL)
}

/**
 * Finds the line of a sign
 * Logic implementation for [findHorizontalLine] and [findVerticalLine]
 * @param extremities the extremities of the sign
 * @param direction the direction of the line
 * @return the points (depending on [direction]) of the sign for creating a line
 */
private fun findLine(extremities: MatOfPoint, direction: LineFinderDirection): Pair<Point, Point>  {
    // Determine if the x or y value of a point should be used based on the direction
    val pointToValue: (Point) -> Double = when (direction) {
        LineFinderDirection.HORIZONTAL -> {
            { it.y }
        }
        LineFinderDirection.VERTICAL -> {
            { it.x }
        }
    }

    val signShape = recognizeShape(extremities)
    val points = extremities.toList()

    val highestPoints = points.sortedBy { pointToValue(it)}.take(2)
    val highestPoint = highestPoints.first()
    val secondHighestPoint = highestPoints.last()

    val lowestPoints = points.sortedByDescending { pointToValue(it) }.take(2)
    val lowestPoint = lowestPoints.first()
    val secondLowestPoint = lowestPoints.last()

    val (highestPointForShape, lowestPointForShape) = findHighAndLowForShape(highestPoint, secondHighestPoint, lowestPoint, secondLowestPoint, signShape, direction)

    return Pair(highestPointForShape, lowestPointForShape
    )
}

/**
 * Interpolates a point between two points
 * @param point1 the first point
 * @param point2 the second point
 */
private fun interpolatePoint(point1: Point, point2: Point): Point {
    return Point((point1.x + point2.x) / 2, (point1.y + point2.y) / 2)
}

/**
 * Find the top and bottom points of a sign based on the shape
 * The shape determines how the points are calculated
 * @param highestPoint the highest point of the sign
 * @param secondHighestPoint the second highest point of the sign
 * @param lowestPoint the lowest point of the sign
 * @param secondLowestPoint the second lowest point of the sign
 * @param signShape the shape of the sign
 * @param direction the direction of the line
 * @return the top and bottom points of the sign
 */
private fun findHighAndLowForShape(highestPoint: Point, secondHighestPoint: Point,
                                     lowestPoint: Point, secondLowestPoint: Point,
                                     signShape: SignShape, direction: LineFinderDirection ): Pair<Point, Point> {
    if (signShape == SignShape.SQUARE || signShape == SignShape.CIRCLE) {
        return Pair(highestPoint, lowestPoint)
    }
    if (signShape == SignShape.OCTAGON) {
        return Pair(
 interpolatePoint(highestPoint, secondHighestPoint),
    interpolatePoint(lowestPoint, secondLowestPoint)
        )
    }
    if (signShape == SignShape.TRIANGLE) {
        if (direction == LineFinderDirection.HORIZONTAL) {
            return Pair(
                interpolatePoint(lowestPoint, secondLowestPoint),
                highestPoint
            )
        }
        if (direction == LineFinderDirection.VERTICAL) {
            return Pair(
                highestPoint,
                interpolatePoint(lowestPoint, secondLowestPoint)
            )
        }
    }
    if (signShape == SignShape.TRIANGLE_FLIPPED) {
        if (direction == LineFinderDirection.HORIZONTAL) {
            return Pair(
                lowestPoint,
                interpolatePoint(highestPoint, secondHighestPoint)
            )
        }
        if (direction == LineFinderDirection.VERTICAL) {
            return Pair(
                interpolatePoint(lowestPoint, secondLowestPoint),
                highestPoint
            )
        }
    }
    throw IllegalArgumentException("Invalid shape")
}