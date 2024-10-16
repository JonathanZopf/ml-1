package org.hszg

import com.google.gson.Gson
import nu.pattern.OpenCV
import org.hszg.sign_analyzer.SignAnalysisException
import org.hszg.sign_analyzer.analyzeSign
import org.hszg.training.TrainingData
import org.hszg.training.readTrainingData
import org.hszg.training.writeTrainingData

fun main() {
    println("Starting sign analysis")
    OpenCV.loadShared()
    val signs = loadSigns()
    signs.forEach { sign ->
        try {
           val signProperties = analyzeSign(sign)
            writeTrainingData(TrainingData(sign.classification, signProperties.toFeatureVector()))
        } catch (e: SignAnalysisException) {
            println(e.message)
            println("An error occurred while analyzing sign "+ sign.path)
        }
    }
}