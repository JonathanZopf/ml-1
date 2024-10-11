package org.hszg.sign_cropper

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

/**
 * Analyzes the sign, crops it based on the contour and makes the background transparent.
 * @param originalSign The original sign image, not cropped.
 * @return The cropped sign with a transparent background.
 */
fun cropSignWithTransparency(originalSign: Mat): Mat {
        // Convert Images to RGBA, Gray and Binary, used for finding the contours and making the outside transparent
        val helperImages = SignCropperConvertedHelperImages(originalSign)

        // Find the largest contour, which should be the outside of the sign
        val largestContour = findSignContour(helperImages)

        // Make the outside of the sign transparent
        val signWithOutsideTransparent = getSignWithOutsideTransparent(helperImages, largestContour)

        // Get the bounding box of the largest contour
        val rect = Imgproc.boundingRect(largestContour)

        // Crop the image to the bounding box (sign)
        return Mat(signWithOutsideTransparent, rect)
}

/**
 * Makes all pixels outside the contour transparent and returns the modified image.
 * @param helperImages The helper images that where converted from the original sign image.
 * @param contour The contour that defines the sign.
 * @return The modified image with a transparent background.
 */
private fun getSignWithOutsideTransparent(helperImages: SignCropperConvertedHelperImages, contour: MatOfPoint) : Mat  {
    val sign = helperImages.getRgba()
    // Create a mask for the sign based on the largest contour
    val mask = Mat.zeros(helperImages.getRgba().size(), CvType.CV_8UC1)
    Imgproc.drawContours(mask, listOf(contour), -1, Scalar(255.0), Imgproc.FILLED)

    // Iterate over each pixel and set alpha to 0 where it's not part of the sign (outside contour)
    for (row in 0 until sign.rows()) {
        for (col in 0 until sign.cols()) {
            val pixelMask = mask.get(row, col)[0]
            if (pixelMask == 0.0) {  // Background area
                helperImages.getRgba().put(row, col, 0.0, 0.0, 0.0, 0.0) // Transparent background
            }
        }
    }

    return sign
}