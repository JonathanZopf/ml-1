package org.hszg

import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import java.io.File

/**
 * Loads all signs for training. Makes 50% of the signs available for training.
 * @return A sequence of [LoadedSign] objects.
 **/
fun loadSignsForTraining() = loadSigns { i -> i % 2 == 0 }

/**
 * Loads all signs for testing. Makes 50% of the signs available for testing.
 * @return A sequence of [LoadedSign] objects.
 **/
fun loadSignsForClassificationTesting() = loadSigns { i -> i % 2 == 1 }

/**
 * Loads all signs for testing. Makes that the a sign is loaded with a probability of probabilityOfSelection.
 */
fun loadSignsForClassificationTesting(probabilityOfSelection: Double) : Sequence<LoadedSign> {
    assert(probabilityOfSelection in 0.0..1.0)
    return loadSigns { _ -> Math.random() < probabilityOfSelection }
}
/**
 * Loads all signs from a specified directory.
 * The location to the parent directory is specified in the resources folder in a file called file_location.txt.
 * The file_location.txt file should contain the absolute path to the parent directory.
 * @param chooseSignFunction A function that decides which signs to load. The function should return true for the signs to load.
 * @return A sequence of [LoadedSign] objects.
 */
fun loadSigns(chooseSignFunction: (Int)->Boolean): Sequence<LoadedSign> {
    // Get the uri of the parent directory from the resources folder
    val classloader = Thread.currentThread().contextClassLoader
    val fileLocationFile = classloader.getResourceAsStream("file_location.txt")
        ?: throw IllegalArgumentException("There is no file_location.txt in the resources folder")

    val parentFolderLocation = fileLocationFile.bufferedReader().use { it.readText() }
    val parentDir = File(parentFolderLocation)

    if (!parentDir.exists() || !parentDir.isDirectory) {
        throw IllegalArgumentException("There is no parent directory in ${parentDir.absolutePath}")
    }

    val childDir = parentDir.listFiles { file -> file.isDirectory }
    if (childDir == null || childDir.isEmpty()) {
        throw IllegalArgumentException("There are no child directories in ${parentDir.absolutePath}")
    }

    var totalIndex = 0
    return sequence {
        for (dir in childDir) {
            val path = dir.absolutePath
            val signClass = SignClassification.valueOf(dir.name)
            val signImages = loadAllSignsInDirLazy(dir)
            signImages.forEachIndexed { index, signImage ->
                if (chooseSignFunction(totalIndex + index)) {
                    return@forEachIndexed yield(
                        LoadedSign(
                            path = path,
                            classification = signClass,
                            image = signImage
                        )
                    )
                }
            }
            totalIndex += signImages.count()

        }
}}

/**
 * Loads all sign images from a specified directory lazily.
 * @param directory The directory to load the signs from.
 * @return A sequence of Mat objects representing the signs.
 */
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