package org.hszg.learner_implementations

import org.hszg.learner_implementations.knn.KNearestNeighborDistanceCalculation
import org.hszg.Learner
import org.hszg.SignClassification
import org.hszg.training.TrainingData

/**
 * A k-nearest neighbor learner.
 * The complexity of the algorithm for classification is O(n*m) with n the number of features and m the number of training data.
 */
class KNearestNeighbor(private val ksToTake: Int, private val distanceCalculation: KNearestNeighborDistanceCalculation) : Learner {

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
            val distance = distanceCalculation.calculateDistance(data.featureVector, featureVector)
            Pair(data.classification, distance)
        }.sortedBy { it.second }

        val kNearest = neighbors.take(ksToTake)
        return kNearest.groupBy { it.first }
            .maxByOrNull { it.value.size }?.key ?: throw IllegalArgumentException("No neighbors found")
    }
}