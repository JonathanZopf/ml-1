package org.hszg.classification

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Summarize the classification results and print them to the console.
 * @param data The data to summarize. Items are true if the classification was correct, false otherwise.
 */
fun summarizeClassificationResults(data: List<Boolean>) {
    val total = data.size
    val success = data.count { it }
    val accuracy = success * 100 / total.toDouble()
    println("")
    println("")
    println("–––––––––––––––Classification Results–––––––––––––––")
    println("Accuracy: $success out of $total cases ($accuracy%)")
    println("Confidence interval: ${calculateConfidenceInterval(data.map { if (it) 1.0 else 0.0 })}")
    println("––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––")
}

/**
 * Calculate the confidence interval for the given data.
 * @param data The data to calculate the confidence interval for.
 * @return A list containing the confidence interval, the lower bound and the upper bound.
 */
private fun calculateConfidenceInterval(data: List<Double>): List<Double> {
    val mean = data.average()
    val n = data.size
    val standardError = sqrt(mean * (1 - mean) / n)
    val z = 1.96  // For 95% confidence level

    val confidenceInterval = z * standardError
    val lowerBound = (mean - confidenceInterval).coerceAtLeast(0.0)
    val upperBound = (mean + confidenceInterval).coerceAtMost(1.0)

    return listOf(confidenceInterval, lowerBound, upperBound)
}