package org.hszg
import org.hszg.sign_properties.SignProperties
import org.hszg.training.TrainingData
import org.hszg.training.readTrainingData
import kotlin.math.sqrt

fun classifySignFromProperties(trainingData: List<TrainingData>, signProperties: SignProperties) : SignClassification {
    val vector = signProperties.toFeatureVector()
    val classification = kNearestNeighbors(trainingData, vector, 3)
    return classification
}

private fun kNearestNeighbors(trainingData: List<TrainingData>, inputVector: List<Double>, k: Int):
        SignClassification {
    val neighbors = trainingData.map { data ->
        val distance = euclidicDistance(data.featureVector, inputVector)
        Pair(data.classification, distance)
    }.sortedBy { it.second }

    val kNearest = neighbors.take(k)
    return kNearest.groupBy { it.first }
        .maxByOrNull { it.value.size }?.key ?: throw IllegalArgumentException("No neigbors found")
}

private fun euclidicDistance(vector1: List<Double>, vector2: List<Double>): Double {
    return sqrt(vector1.zip(vector2).sumOf { (v1, v2) -> (v1 - v2) * (v1 - v2) })
}
