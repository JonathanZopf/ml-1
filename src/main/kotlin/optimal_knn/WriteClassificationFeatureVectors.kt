package org.hszg.optimal_knn

import com.google.gson.Gson
import nu.pattern.OpenCV
import org.hszg.SignLoading.getAllSignsForTrainingAndClassification
import org.hszg.sign_analyzer.analyzeSign
import org.hszg.training.TrainingData
import java.io.File

/**
 * This function will write the classification and feature vectors of signs to a file. This can save time when running the analysis multiple times.
 */
fun main() {
    OpenCV.loadLocally()
    val count = 3000
    print("This function will write the classification and feature vectors of signs to a file. This can save time when running the analysis multiple times.")

    val signsForAnalysis = getAllSignsForTrainingAndClassification(0, count).getSignsForClassification()
    val trainingData = mutableSetOf<TrainingData>()
    for (sign in signsForAnalysis) {
        try {
            println("Starting analysis of sign " + sign.getMinimalPath())
            val signProperties = analyzeSign(sign, false)
            trainingData.add(TrainingData(sign.getClassification(), signProperties.toFeatureVector()))
        } catch (e: Exception) {
            println("⚠️An error occurred while analyzing sign " + sign.getMinimalPath() + "⚠️")
            println("⚠️The analysis of the sign will be skipped⚠️")
        }
    }

    println("Writing to file")
    writeToFile(trainingData)
    println("Finished writing to file")
}

fun readClassificationFeatureVectorsFromFile(): Set<TrainingData> {
    val file = getFile()
    val gson = Gson()
    return file.readLines().map { line -> gson.fromJson(line, TrainingData::class.java) }.toSet()
}

private fun writeToFile(data: Set<TrainingData>) {
    val gson = Gson()
    val file = getFile()
    file.writeText("")
    data.forEach { (classification, featureVector) ->
        file.appendText(gson.toJson(TrainingData(classification, featureVector)) + "\n")
    }
}


private fun getFile() : File{
    val classloader = Thread.currentThread().contextClassLoader
    val fileLocationFile = classloader.getResourceAsStream("classification_vectors.txt")
        ?: throw IllegalArgumentException("There is no classification_vectors in the resources folder")
    val fileLocation = fileLocationFile.bufferedReader().use { it.readText() }
    val file = File(fileLocation)
    if (!file.exists()) {
        throw IllegalArgumentException("Invalid file location: $fileLocation")
    }
    return file
}