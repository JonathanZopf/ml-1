package org.hszg.sign_cropper

import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

/**
 * This class is used to convert the original image to different formats that are useful for the sign cropper.
 * @param original The original image of the sign.
 * @property rgba The original image converted to RGBA.
 * @property binary The original image converted to a binary image.
 */
class SignCropperConvertedHelperImages(original: Mat) {
    // Convert the original image to RGBA (with an alpha channel for transparency)
    private val rgba: Mat = Mat()
    private val binary: Mat = Mat()

    init {
        Imgproc.cvtColor(original, rgba, Imgproc.COLOR_RGB2RGBA)
        // Create a grayscale image in order to generate the binary image
        val gray = Mat()
        Imgproc.cvtColor(original, gray, Imgproc.COLOR_RGB2GRAY)
        // Apply a binary threshold to get a binary image (foreground vs background)
        Imgproc.threshold(gray, binary, 240.0, 255.0, Imgproc.THRESH_BINARY_INV)
    }

    fun getRgba(): Mat = rgba
    fun getBinary(): Mat = binary
}
