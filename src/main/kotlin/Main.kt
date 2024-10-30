package org.hszg

import nu.pattern.OpenCV
import org.hszg.SignLoading.getAllSignsForTrainingAndClassification
import org.hszg.classification.evaluateSignClassification
import org.hszg.learner_implementations.DecisionTree
import org.hszg.learner_implementations.KNearestNeighbor
import org.hszg.sign_analyzer.SignAnalysisException
import org.hszg.sign_analyzer.analyzeSign
import org.hszg.training.TrainingData
import org.hszg.training.readTrainingData
import org.hszg.training.writeTrainingData

fun main() {
    OpenCV.loadLocally()
    println("Getting all images ...")
    val signs = getAllSignsForTrainingAndClassification(50, 50)
    println("Do you want to train the model(t) or classify the model (c)?")
    val input = readlnOrNull()
    when (input) {
        "t" -> {
            signs.getSignsForTraining().forEach {
                try {
                    val signProperties = analyzeSign(it)
                    writeTrainingData(TrainingData(it.getClassification(), signProperties.toFeatureVector()))
                } catch (e: SignAnalysisException) {
                    println(e.message)
                    println("An error occurred while analyzing sign "+ it.getAbsolutePath())
                }
            }
        }
        "c" -> {
            val trainingData = readTrainingData().toSet()
            println("Do you want to use the kNearestNeighbor-Algorithm(k) or the Decision-Tree-Algorithm(d)?")
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
                else -> {
                    println("Invalid input")
                    return
                }
            }
            learnerImplementation.learn(trainingData)
            val classificationSigns = signs.getSignsForClassification()
            classificationSigns.forEach { sign ->
                println("–––––––––––––––Starting classification of sign ${sign.getAbsolutePath()}––––––––––––––––––")
                try {
                    val signProperties = analyzeSign(sign)
                    val classification = learnerImplementation.classify(signProperties.toFeatureVector())
                    println("Sign ${sign.getAbsolutePath()} is classified as $classification")
                    if (classification == sign.getClassification()) {
                        println("Sign ${sign.getAbsolutePath()} was classified correctly")
                    } else {
                        println("Sign ${sign.getAbsolutePath()} was classified wrongly as $classification, while the actual type " +
                                "is ${sign.getClassification()}")
                    }
                } catch (e: SignAnalysisException) {
                    println(e.message)
                    println("An error occurred while analyzing sign " + sign.getAbsolutePath())
                }
                println("–––––––––––––––Finished classification of sign ${sign.getAbsolutePath()}––––––––––––––––––")
            }
            evaluateSignClassification(classificationSigns, classificationSigns.map { it.getClassification() })
        }
        else -> {
            println("Invalid input")
        }
}}