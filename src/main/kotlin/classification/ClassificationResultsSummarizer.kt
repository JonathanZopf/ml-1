package org.hszg.classification

import kotlin.math.pow
import kotlin.math.sqrt

fun summarizeClassificationResults(data: List<Boolean>) {
    val truePositives = data.count { it }
    val falsePositives = data.size - truePositives
    val trueNegatives = 0
    val falseNegatives = 0

    val accuracy = (truePositives + trueNegatives) / data.size.toDouble()
    val precision = truePositives / (truePositives + falsePositives).toDouble()
    val recall = truePositives / (truePositives + falseNegatives).toDouble()

    println("")
    println("")
    println("–––––––––––––––Classification Results–––––––––––––––")
    println("Accuracy: $accuracy")
    println("Precision: $precision")
    println("Recall: $recall")
    println("Confidence interval: ${calculateConfidenceInterval(data.map { if (it) 1.0 else 0.0 })}")
    println("––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––")
}
    private fun calculateConfidenceInterval(data: List<Double>): List<Double>{
        val mean = data.average()
        val standardDeviation = sqrt(data.sumOf {
            (it - mean).pow(2.0)
        } / data.size)
        val standardError = standardDeviation / sqrt(data.size.toDouble())
        val z = 1.96

        val confidenceInterval = z * standardError
        val lowerBound = mean - confidenceInterval
        val upperBound = mean + confidenceInterval

        return listOf(confidenceInterval, lowerBound, upperBound)
    }