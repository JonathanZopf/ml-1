package org.hszg

import nu.pattern.OpenCV
import org.hszg.SignLoading.getAllSignsForTrainingAndClassification
import org.hszg.classification.summarizeClassificationResults
import org.hszg.learner_implementations.DecisionTree
import org.hszg.learner_implementations.KNearestNeighbor
import org.hszg.learner_implementations.NeuronalNetwork
import org.hszg.sign_analyzer.SignAnalysisException
import org.hszg.sign_analyzer.analyzeSign
import org.hszg.training.TrainingData
import org.hszg.training.readTrainingData
import org.hszg.training.writeTrainingData

fun main() {
    OpenCV.loadLocally()
    println("Getting all images ...")
    val signs = getAllSignsForTrainingAndClassification(1000, 500)
    println("Do you want to train the model(t) or classify the model (c)?")
    val input = readlnOrNull()
    when (input) {
        "t" -> {
            signs.getSignsForTraining().forEach {
                try {
                    val signProperties = analyzeSign(it, true)
                    writeTrainingData(TrainingData(it.getClassification(), signProperties.toFeatureVector()))
                } catch (e: SignAnalysisException) {
                    println(SignAnalysisException("An error occurred while analyzing sign "+ it.getMinimalPath()))
                }
            }
        }
        "c" -> {
            val trainingData = readTrainingData().toSet()
            println("Do you want to use the kNearestNeighbor-Algorithm(k), the Decision-Tree-Algorithm(d) or the Neuronal-Network (n)?")
            val inputClassification = readlnOrNull()
            val learnerImplementation: Learner =
            when (inputClassification) {
                "k" -> {
                    val learner = KNearestNeighbor()
                    learner
                }
                "d" -> {
                    val learner = DecisionTree()
                    learner
                }
                "n" -> {
                    val learner = NeuronalNetwork()
                    learner
                }
                else -> {
                    println("Invalid input")
                    return
                }
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
                    val signProperties = analyzeSign(sign, true)
                    val classification = learnerImplementation.classify(signProperties.toFeatureVector())
                    println("Sign ${sign.getMinimalPath()} is classified as $classification")
                    if (classification == sign.getClassification()) {
                        println("✅Sign was classified correctly")
                        correctIdentifications.add(true)
                    } else {
                        println("❌Sign was classified wrongly (actual type: ${sign.getClassification()})")
                        correctIdentifications.add(false)
                    }
                } catch (e: SignAnalysisException) {
                    throw e
                    println(e.message)
                    println("!!!An error occurred while analyzing sign " + sign.getMinimalPath() + "!!!")
                    println("!!!The analysis of the sign will be skipped, but counted as falsely identified!!!")
                    correctIdentifications.add(false)
                }
                println("–––––––––––––––Finished classification of sign ${sign.getMinimalPath()}––––––––––––––––––")
            }
            summarizeClassificationResults(correctIdentifications)
        }
        else -> {
            println("Invalid input")
        }
}}