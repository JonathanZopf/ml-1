package org.hszg.SignLoading

import org.hszg.SignClassification
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

/**
 * Represents a sign that can be loaded from the file system.
 * @param absolutePath The absolute path to the image file.
 * @param classification The classification of the sign.
 */
class LoadableSign(private val absolutePath: String, private val classification: SignClassification) {
    /**
     * Loads the image from the file system based on the absolute path and converts it to RGBA
     * @return The image as a [Mat] object.
     */
    fun loadImage(): Mat {
        val image = Imgcodecs.imread(absolutePath)
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGBA)
        return image
    }

    /**
     * Get the classification of the sign.
     * @return The classification of the sign.
     */
    fun getClassification(): SignClassification {
        return classification
    }

    /**
     * Get the absolute path to the image file.
     * @return The absolute path to the image file.
     */
    fun getAbsolutePath(): String {
        return absolutePath
    }

    /**
     * Get the minimal path of the image file which looks better in logs.
     * Is the third last part (delimiter /) of the absolute path.
     * @return The minimal path of the image file.
     */
    fun getMinimalPath(): String {
        val parts = absolutePath.split("/")
        if (parts.size < 5) {
            println("!!!Path $absolutePath is too short to get a minimal path!!!")
            return absolutePath
        }
        return absolutePath.substringAfter(parts[parts.size - 5])
    }
}