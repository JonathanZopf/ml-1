package org.hszg.learner_implementations

import org.hszg.Learner
import org.hszg.SignClassification
import org.hszg.training.TrainingData
import kotlin.math.sqrt

/**
 * A k-nearest neighbor learner.
 */
class KNearestNeighbor : Learner {
   private val ksToTake = 3
    /**
     * The training data the learner has learned from.
     */
    private var trainingData: Set<TrainingData> = emptySet()
    override fun learn(trainingData: Set<TrainingData>) {
        this.trainingData = trainingData
    }

    override fun classify(featureVector: List<Double>): SignClassification {
        if (trainingData.isEmpty()) {
            throw IllegalStateException("No training data available")
        }

        val neighbors = trainingData.map { data ->
            val distance = euclideanDistance(data.featureVector, featureVector)
            Pair(data.classification, distance)
        }.sortedBy { it.second }

        val kNearest = neighbors.take(ksToTake)
        return kNearest.groupBy { it.first }
            .maxByOrNull { it.value.size }?.key ?: throw IllegalArgumentException("No neighbors found")
    }

    /**
     * Calculate the Euclidean distance between two vectors.
     */
    private fun euclideanDistance(vector1: List<Double>, vector2: List<Double>): Double {
        return sqrt(vector1.zip(vector2).sumOf { (v1, v2) -> (v1 - v2) * (v1 - v2) })
    }
}