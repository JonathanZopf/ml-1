package org.hszg.sign_analyzer

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.imgproc.Imgproc

/**
 * Find the extremities of a traffic sign.
 * The extremities are the corners of the traffic sign which gives a rough idea of the shape of the sign.
 * The function uses Canny edge detection to find the edges of the traffic sign and then approximates the polygon to account for rounded corners.
 * @param inputImage The input image of the traffic sign. Should not be cropped because it doesnt work with cropped images.
 * @return The extremities of the traffic sign.

 */
fun findExtremities(inputImage: Mat): MatOfPoint {
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