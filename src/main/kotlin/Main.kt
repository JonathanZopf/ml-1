package org.hszg

import nu.pattern.OpenCV
import org.hszg.classification.classificationTest
import org.hszg.learner_implementations.KNearestNeighbor
import org.hszg.sign_analyzer.SignAnalysisException
import org.hszg.sign_analyzer.analyzeSign
import org.hszg.training.TrainingData
import org.hszg.training.readTrainingData
import org.hszg.training.writeTrainingData

fun main() {
    OpenCV.loadLocally()
    println("Do you want to train the model(t) or classify the model (c)?")
    val input = readlnOrNull()
    when (input) {
        "t" -> {
            val signs = loadSignsForTraining()
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
        "c" -> {
            val signs = loadSignsForTesting()
            val trainingData = readTrainingData().toSet()
            var successfullyClassifiedSignCount = 0
            var totalSignCount = 0

            val learnerImplementation = KNearestNeighbor()
            learnerImplementation.learn(trainingData)

            signs.forEach { sign ->
                try {
                    val signProperties = analyzeSign(sign)
                    val classification = learnerImplementation.classify(signProperties.toFeatureVector())
                    println("Sign ${sign.path} is classified as $classification")
                    if (classification == sign.classification) {
                        successfullyClassifiedSignCount++
                        println("Sign ${sign.path} was classified correctly")
                    } else {
                        println("Sign ${sign.path} was classified wrongly as $classification, while the actual type is ${sign.classification}")
                    }
                    totalSignCount++
                    println("Successfully classified $successfullyClassifiedSignCount out of $totalSignCount signs (${(successfullyClassifiedSignCount.toDouble() / totalSignCount.toDouble()) * 100}%)c")
                } catch (e: SignAnalysisException) {
                    println(e.message)
                    println("An error occurred while analyzing sign " + sign.path)
                }
            }
        }
        else -> {
            println("Invalid input")
        }
}}