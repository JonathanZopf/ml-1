package org.hszg

import com.google.gson.Gson
import nu.pattern.OpenCV
import org.hszg.classification.classificationTest
import org.hszg.classification.kNearestNeighbors
import org.hszg.sign_analyzer.SignAnalysisException
import org.hszg.sign_analyzer.analyzeSign
import org.hszg.training.TrainingData
import org.hszg.training.readTrainingData
import org.hszg.training.writeTrainingData

fun main() {
    println("Starting sign analysis")
    OpenCV.loadLocally()
    val signs = loadSigns()
//    val classificationTest = classificationTest()
    signs.forEach { sign ->
        try {
            val signProperties = analyzeSign(sign)
            /**
             * writing training data
             */
//            writeTrainingData(TrainingData(sign.classification, signProperties.toFeatureVector()))
//            println(signProperties.toFeatureVector())
            /**
             * classify with k nearest neighbors
             */
            val trainingData = readTrainingData()
            val inputVector = signProperties.toFeatureVector()
            val k = 3
            val classification = kNearestNeighbors(trainingData, inputVector, k)
            println("Sign ${sign.path} is classified as $classification")
//            classificationTest.setTestData(sign, classification)
//            classificationTest.testSignClassification()

        } catch (e: SignAnalysisException) {
            println(e.message)
            println("An error occurred while analyzing sign "+ sign.path)
        }
    }
}