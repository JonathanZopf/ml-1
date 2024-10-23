package org.hszg

import org.hszg.training.TrainingData

/**
 * Interface for a learner that can learn from training data and classify feature vectors.
 */
interface Learner {
    /**
     * Make the system learn from the given training data.
     * @param trainingData The training data to learn from.
     */
    fun learn(trainingData: Set<TrainingData>)

    /**
     * Classify the given feature vector.
     * The classification is dependent on the learned data. Therefore learn() must be called beforehand.
     * @param featureVector The feature vector of the sign to classify.
     * @return The classification of the sign.
     */
    fun classify(featureVector: List<Double>): SignClassification
}