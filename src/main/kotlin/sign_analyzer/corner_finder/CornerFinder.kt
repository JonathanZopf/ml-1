package org.hszg.sign_analyzer.extremities_finder

import org.hszg.sign_analyzer.SignAnalysisException
import org.hszg.sign_analyzer.corner_finder.OutermostCorners
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

/**
 * Find the corners of a traffic sign.
 *
 * This algorithm is good at finding as many corners as possible but the results might be inaccurate.
 *
 * The corners of the traffic sign give a rough idea of the shape of the sign.
 * The function uses Canny edge detection to find the edges of the traffic sign and then approximates the polygon to account for rounded corners.
 * @param inputImage The input image of the traffic sign. Should not be cropped because it doesnt work with cropped images.
 * @return The extremities of the traffic sign.
 */
fun findCorners(inputImage: Mat): MatOfPoint {
    // Convert the image to binary based on alpha channel
    val gray = Mat()
    Imgproc.cvtColor(inputImage, gray, Imgproc.COLOR_BGRA2GRAY)

    // Apply Canny edge detection on the binary image
    val edges = Mat()
    Imgproc.Canny(gray, edges, 50.0, 150.0)

    // Find contours
    val contours = ArrayList<MatOfPoint>()
    val hierarchy = Mat()
    Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

    // Assume the largest contour is the traffic sign
    var largestContour: MatOfPoint? = null
    var largestArea = Double.MIN_VALUE
    for (contour in contours) {
        val area = Imgproc.contourArea(contour)
        if (area > largestArea) {
            largestArea = area
            largestContour = contour
        }
    }

    // If no contour was found, return an empty MatOfPoint
    if (largestContour == null) {
       throw SignAnalysisException("No contour found")
    }

    // Convert MatOfPoint (integer) to MatOfPoint2f (floating point)
    val largestContour2f = MatOfPoint2f(*largestContour!!.toArray())

    // Approximate the polygon to reduce rounded corners
    val epsilon = 0.02 * Imgproc.arcLength(largestContour2f, true)
    val approxCurve2f = MatOfPoint2f()
    Imgproc.approxPolyDP(largestContour2f, approxCurve2f, epsilon, true)

    // Convert the result back to MatOfPoint (integer points)
    val result = MatOfPoint()
    approxCurve2f.convertTo(result, CvType.CV_32S)

    return result
}

/**
 * Find the outermost corners of a sign.
 * This function is well suited for finding the vertical line of a sign.
 * @param contour The contour of the sign.
 * @return The top and bottom point of the sign for creating a line.
 */
fun findOutermostCorners(contour: MatOfPoint): OutermostCorners {
    val points = contour.toList()
    val topPoints = points.sortedBy { it.y }
    val bottomPoints = points.sortedBy { -it.y }
    val leftPoints = points.sortedBy { it.x }
    val rightPoints = points.sortedBy { -it.x }

    if (topPoints.isEmpty()) {
        throw IllegalArgumentException("There are no top points")
    }

    if (bottomPoints.isEmpty()) {
        throw IllegalArgumentException("There are no bottom points")
    }

    if (leftPoints.isEmpty()) {
        throw IllegalArgumentException("There are no left points")
    }

    if (rightPoints.isEmpty()) {
        throw IllegalArgumentException("There are no right points")
    }

    return OutermostCorners(
        top = topPoints.first(),
        bottom = bottomPoints.first(),
        left = leftPoints.first(),
        right = rightPoints.first()
    )
}