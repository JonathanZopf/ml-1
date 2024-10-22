package org.hszg.classification

import org.hszg.SignClassification
import org.hszg.training.TrainingData
import kotlin.math.sqrt


fun kNearestNeighbors(trainingData: List<TrainingData>, inputVector: List<Double>, k: Int):
SignClassification {
    val neighbors = trainingData.map { data ->
        val distance = euclidicDistance(data.featureVector, inputVector)
        Neighbor(data.classification, distance)
    }.sortedBy { it.distance }

    val kNearest = neighbors.take(k)
    return kNearest.groupBy { it.classification }
        .maxByOrNull { it.value.size }?.key ?: throw IllegalArgumentException("No neigbors found")
}

fun euclidicDistance(vector1: List<Double>, vector2: List<Double>): Double {
    return sqrt(vector1.zip(vector2).sumOf { (v1, v2) -> (v1 - v2) * (v1 - v2) })
}
