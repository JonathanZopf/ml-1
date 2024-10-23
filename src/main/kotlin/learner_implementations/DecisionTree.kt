package org.hszg.learner_implementations

import org.hszg.Learner
import org.hszg.SignClassification
import org.hszg.training.TrainingData

class DecisionTree : Learner {
    var successor: Map<Number, DecisionTree> = mapOf()
    var classification: SignClassification? = null
    var depth: Int = 0

    override fun learn(trainingData: Set<TrainingData>) {
        TODO("Not yet implemented")
    }

    override fun classify(featureVector: List<Double>): SignClassification {
        TODO("Not yet implemented")
    }
}