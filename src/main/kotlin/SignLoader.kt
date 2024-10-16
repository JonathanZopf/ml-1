package org.hszg

import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import java.io.File

fun loadSigns(): Sequence<LoadedSign> {
    val parentDir = File("/Users/jonathan/Downloads/Verkehrszeichen Complete")

    if (!parentDir.exists() || !parentDir.isDirectory) {
        throw IllegalArgumentException("There is no parent directory in ${parentDir.absolutePath}")
    }

    val childDir = parentDir.listFiles { file -> file.isDirectory }
    if (childDir == null || childDir.isEmpty()) {
        throw IllegalArgumentException("There are no child directories in ${parentDir.absolutePath}")
    }

    return sequence {
        for (dir in childDir) {
            val path = dir.absolutePath
            val signClass = SignClassification.valueOf(dir.name)
            val signImages = loadAllSignsInDirLazy(dir)
            signImages.forEach { signImage -> yield(LoadedSign(
                path = path,
                classification = signClass,
                image = signImage
            )) }
        }
}}

fun loadAllSignsInDirLazy(directory: File): Sequence<Mat> {
    if (!directory.exists() || !directory.isDirectory) {
        throw IllegalArgumentException("Invalid directory: ${directory.absolutePath}")
    }

    return sequence {
        directory.walkTopDown() // Traverse the directory tree recursively
            .filter { file -> file.isFile && file.extension in listOf("jpg", "png", "bmp") }
            .forEach { file ->
                val mat = Imgcodecs.imread(file.absolutePath)
                if (!mat.empty()) {
                    yield(mat)  // Yield each Mat image lazily
                }
            }
    }
}