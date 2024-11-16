package org.hszg.sign_analyzer.line_finder

import org.hszg.sign_analyzer.shape_recognizer.SignShape
import org.hszg.sign_analyzer.shape_recognizer.recognizeShape
import org.opencv.core.MatOfPoint
import org.opencv.core.Point

/**
 * Finds the vertical line of a sign
 * Can be used for color analysis
 * @param extremities the extremities of the sign
 * @return the top and bottom point of the sign for creating a line
 */
fun findVerticalLine(extremities: MatOfPoint) : Pair<Point, Point>{
    val points = extremities.toList()

    val highestPoints = points.sortedBy {it.y}.take(2)
    val highestPoint = highestPoints.first()

    val lowestPoints = points.sortedByDescending { it.y }.take(2)
    val lowestPoint = lowestPoints.first()

    return Pair(highestPoint, lowestPoint)
}