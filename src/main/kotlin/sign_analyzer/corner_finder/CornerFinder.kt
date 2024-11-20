package org.hszg.sign_analyzer.extremities_finder

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.sqrt

/**
 * Find the corners from the cropping contour.
 * The corners are the extreme points of the sign. They give a rough idea of the shape of the sign.
 *
 * @param contourFromCropping The contour of the sign.
 * @return The corners of the sign.
 */
fun findCorners(contourFromCropping: MatOfPoint): List<Point> {
    // Convert MatOfPoint (integer) to MatOfPoint2f (floating point)
    val contour2f = MatOfPoint2f(*contourFromCropping.toArray())

    val size = sqrt(contour2f.size().area())

    // Approximate the polygon to reduce rounded corners
    val epsilon = Imgproc.arcLength(contour2f, true) / (3* size)
    val approxCurve2f = MatOfPoint2f()
    Imgproc.approxPolyDP(contour2f, approxCurve2f, epsilon, true)

    // Convert the result back to MatOfPoint (integer points)
    val result = MatOfPoint()
    approxCurve2f.convertTo(result, CvType.CV_32S)

    return result.toList()
}