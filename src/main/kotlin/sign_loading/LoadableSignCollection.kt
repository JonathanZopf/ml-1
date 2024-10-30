package org.hszg.SignLoading

/**
 * Represents a collection of signs that can be loaded from the file system.
 * @param signsForTraining The signs that should be used for training.
 * @param signsForClassification The signs that should be used for classification.
 */
class LoadableSignCollection(private val signsForTraining: List<LoadableSign>, private val signsForClassification:  List<LoadableSign>) {
    fun getSignsForTraining(): List<LoadableSign> {
        return signsForTraining
    }

    fun getSignsForClassification(): List<LoadableSign> {
        return signsForClassification
    }
}