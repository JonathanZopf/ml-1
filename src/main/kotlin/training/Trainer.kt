package org.hszg.training

import com.google.gson.Gson
import org.hszg.SignClassification
import java.io.File

fun writeTrainingData(trainingData: TrainingData) {
    val gson = Gson()
    getTrainingFile().appendText(gson.toJson(trainingData) + "\n")
}

fun readTrainingData(): List<TrainingData> {
    val gson = Gson()
    return getTrainingFile().readLines().map { line -> gson.fromJson(line, TrainingData::class.java) }
}

private fun getTrainingFile(): File {
    val classloader = Thread.currentThread().contextClassLoader
    val fileLocationFile = classloader.getResourceAsStream("training_data_location.txt")
        ?: throw IllegalArgumentException("There is no training_data_location.txt in the resources folder")
    val fileLocation = fileLocationFile.bufferedReader().use { it.readText() }

    val file = File(fileLocation)
    if (!file.exists()) {
        throw IllegalArgumentException("Invalid file location: $fileLocation")
    }
    return file
}