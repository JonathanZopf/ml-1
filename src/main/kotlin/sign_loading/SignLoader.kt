package org.hszg.SignLoading

import org.hszg.SignClassification
import java.io.File

/**
 * Gets all signs that are available and splits them into training and classification signs.
 * @param numberTrainingSigns The number of signs that should be used for training.
 * @param numberClassificationSigns The number of signs that should be used for classification.
 */
fun getAllSignsForTrainingAndClassification(numberTrainingSigns: Int, numberClassificationSigns: Int): LoadableSignCollection {
    val allSigns = getAllSigns()
    if (allSigns.size < numberTrainingSigns + numberClassificationSigns) {
        throw IllegalArgumentException("There are not enough signs to split them into training and classificatiion signs.")
    }

    val shuffledSigns = allSigns.shuffled()
    return LoadableSignCollection(shuffledSigns.take(numberTrainingSigns), shuffledSigns.takeLast(numberClassificationSigns))
}

/**
 * Gets all signs unfiltered from the file system.
 * @return A list of all signs.
 */
private fun getAllSigns(): List<LoadableSign> {
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

    val allSigns = mutableListOf<LoadableSign>()
    childDir.forEach { dir ->
        allSigns.addAll(getAllLoadableFilesInDir(dir).map { LoadableSign(it, SignClassification.valueOf(dir.name)) })
    }
    return allSigns
}

/**
    * Gets all paths of loadable signs in a directory.
 * @param directory The directory to search for loadable signs.
 * @return A List of paths to loadable signs.
 */
fun getAllLoadableFilesInDir(directory: File): List<String> {
    if (!directory.exists() || !directory.isDirectory) {
        throw IllegalArgumentException("Invalid directory: ${directory.absolutePath}")
    }

    val allFiles = directory.walkTopDown().filter { file -> file.isFile && file.extension in listOf("jpg", "png", "bmp") }.toList()
    return allFiles.map {it.absolutePath}
}