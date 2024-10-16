package org.hszg.sign_analyzer.shape_recognizer

import org.opencv.core.MatOfPoint
import kotlin.math.abs

/**
 * Recognizes the shape of a sign based on the number of extremities
 * @param extremities the extremities of the sign
 * @return the recognized shape
 */
fun recognizeShape(extremities: MatOfPoint) : SignShape {
    return when (extremities.toList().size) {
        3 -> recognizeTriangleType(extremities)
        4 -> SignShape.SQUARE
        8 -> SignShape.OCTAGON
        else -> SignShape.CIRCLE
    }
}

/**
 * Recognizes the direction (normal or flipped) of a triangle based on the extremities
 * @param extremities the extremities of the sign
 * @return the recognized shape
 */
private fun recognizeTriangleType(extremities: MatOfPoint) : SignShape{
    val points = extremities.toList()
    val topPoints = points.sortedBy { -it.y }
    if (topPoints.size != 3) {
       throw IllegalArgumentException("Invalid number of points for triangle")
    }
    if (abs(topPoints[0].y - topPoints[1].y) < abs(topPoints[1].y - topPoints[2].y)) {
        return SignShape.TRIANGLE
    }
    return SignShape.TRIANGLE_FLIPPED
}