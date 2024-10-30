package org.hszg.SignLoading

import org.hszg.SignClassification
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs

/**
 * Represents a sign that can be loaded from the file system.
 * @param absolutePath The absolute path to the image file.
 * @param classification The classification of the sign.
 */
class LoadableSign(private val absolutePath: String, private val classification: SignClassification) {
    /**
     * Loads the image from the file system based on the absolute path.
     * @return The image as a [Mat] object.
     */
    fun loadImage(): Mat {
        return Imgcodecs.imread(absolutePath)
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
}