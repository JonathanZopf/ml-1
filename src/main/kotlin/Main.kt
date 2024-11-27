package org.hszg

import nu.pattern.OpenCV
import org.hszg.SignLoading.getAllSignsForTrainingAndClassification
import org.hszg.classification.summarizeClassificationResults
import org.hszg.learner_implementations.DecisionTree
import org.hszg.learner_implementations.KNearestNeighbor
import org.hszg.learner_implementations.NeuronalNetwork
import org.hszg.sign_analyzer.analyzeSign
import org.hszg.training.TrainingData
import org.hszg.training.readTrainingData
import org.hszg.training.writeTrainingData
import kotlin.math.sqrt

fun main() {
    OpenCV.loadLocally()
    println("Getting all images ...")
    println("Welcome. Do you want to run the program in debug mode? This will stop the program if an error occurs and write debug images. Press y for yes and any other key for no.")
    val debugMode = readlnOrNull() == "y"
    val signs = getAllSignsForTrainingAndClassification(10000, 500)

    var input: String? = null
    while (input !in listOf("t", "c")) {
        println("Do you want to train the model(t) or classify the model (c)?")
        input = readlnOrNull()
        if (input !in listOf("t", "c")) {
            println("Invalid input, please try again.")
        }
    }

    when (input) {
        "t" -> {
            signs.getSignsForTraining().forEach {
                try {
                    println("Starting analysis of sign " + it.getMinimalPath())
                    val signProperties = analyzeSign(it, debugMode)
                    println("Finished analysis of sign " + it.getMinimalPath() + ", now writing training data")
                    writeTrainingData(TrainingData(it.getClassification(), signProperties.toFeatureVector()))
                } catch (e: Exception) {
                    if (debugMode) {
                        throw e
                    }
                    println("⚠️An error occurred while analyzing sign " + it.getMinimalPath() + "⚠️")
                    println("⚠️The analysis of the sign will be skipped⚠️")
                }
            }
        }
        "c" -> {
            val trainingData = readTrainingData().toSet()

            var inputClassification: String? = null
            while (inputClassification !in listOf("k", "d", "n")) {
                println("Do you want to use the kNearestNeighbor-Algorithm(k), the Decision-Tree-Algorithm(d) or the Neuronal-Network (n)?")
                inputClassification = readlnOrNull()
                if (inputClassification !in listOf("k", "d", "n")) {
                    println("Invalid input, please try again.")
                }
            }

            val learnerImplementation: Learner = when (inputClassification) {
                "k" -> KNearestNeighbor(3) { a, b ->
                    sqrt(a.zip(b).map { (a, b) -> (a - b) * (a - b) }.sum())
                }
                "d" -> DecisionTree()
                "n" -> NeuronalNetwork()
                else -> throw IllegalStateException("This should never happen!")
            }

            println("Starting training of the model")
            learnerImplementation.learn(trainingData)
            println("Finished training of the model, proceeding to classification")

            val classificationSigns = signs.getSignsForClassification()
            val correctIdentifications = mutableListOf<Boolean>()
            classificationSigns.forEach { sign ->
                println()
                println("–––––––––––––––Starting classification of sign ${sign.getMinimalPath()}––––––––––––––––––")
                try {
                    val signProperties = analyzeSign(sign, debugMode)
                    val classification = learnerImplementation.classify(signProperties.toFeatureVector())
                    println("Sign ${sign.getMinimalPath()} is classified as $classification")
                    if (classification == sign.getClassification()) {
                        println("✅Sign was classified correctly")
                        correctIdentifications.add(true)
                    } else {
                        println("❌Sign was classified wrongly (actual type: ${sign.getClassification()})")
                        correctIdentifications.add(false)
                    }
                } catch (e: Exception) {
                    if (debugMode) {
                        throw e
                    }
                    println("⚠️An error occurred while analyzing sign " + sign.getMinimalPath() + "⚠️")
                    println("⚠️The analysis of the sign will be skipped, but counted as falsely identified⚠️")
                    correctIdentifications.add(false)
                }
                println("–––––––––––––––Finished classification of sign ${sign.getMinimalPath()}––––––––––––––––––")
            }
            summarizeClassificationResults(correctIdentifications)
        }
    }
}
