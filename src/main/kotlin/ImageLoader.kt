package org.hszg

import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import java.io.File

/**
 * Loads images from a directory lazily.
 * Loaded lazily means that the images are not loaded into memory until they are accessed, which can save memory.
 * @param directory The directory to load the images from.
 * @return A sequence of Mats, each representing an image.
 */
fun loadImagesLazily(directory: String): Sequence<Mat> {
    val dir = File(directory)

    if (!dir.exists() || !dir.isDirectory) {
        throw IllegalArgumentException("Invalid directory: $directory")
    }

    return dir.listFiles { file -> file.isFile && file.extension in listOf("jpg", "png", "bmp") }
        ?.asSequence()
        ?.map { file -> Imgcodecs.imread(file.absolutePath) }
        ?: emptySequence()
}