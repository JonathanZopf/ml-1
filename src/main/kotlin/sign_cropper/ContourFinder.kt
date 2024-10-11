package org.hszg.sign_cropper

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.imgproc.Imgproc

/**
 * Find the largest contour of the sign
 * @param helperImages The helper images for the sign, used for finding the contours.
 */
fun findSignContour(helperImages: SignCropperConvertedHelperImages) : MatOfPoint {
    // Find contours (boundaries) in the binary image.
    val contours = ArrayList<MatOfPoint>()
    // 'hierarchy' stores information about the image topology but is not needed here.
    val hierarchy = Mat()
    // RETR_EXTERNAL retrieves only the outermost contours
    // CHAIN_APPROX_SIMPLE compresses horizontal, vertical, and diagonal segments into end points to save memory.
    Imgproc.findContours(helperImages.getBinary(), contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)


    // From all detected contours, iterate to find the one with the largest area.
    var largestContour: MatOfPoint? = null
    var maxArea = 0.0
    for (contour in contours) {
        val area = Imgproc.contourArea(contour)
        if (area > maxArea) {
            maxArea = area
            largestContour = contour
        }
    }

    if (largestContour == null) {
        throw IllegalArgumentException("No contour found")
    }

    return largestContour
}