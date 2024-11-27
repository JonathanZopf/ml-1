package org.hszg.learner_implementations.knn

/**
 * Interface for the calculation of the distance between two feature vectors.
 */
fun interface KNearestNeighborDistanceCalculation {
    /**
     * Calculate the distance between two feature vectors.
     */
    fun calculateDistance(vector1: List<Double>, vector2: List<Double>): Double
}