package org.hszg.learner_implementations

import org.hszg.Learner
import org.hszg.SignClassification
import org.hszg.learner_implementations.neuronal_network.Model
import org.hszg.learner_implementations.neuronal_network.Perceptron
import org.hszg.learner_implementations.neuronal_network.classificationToModel
import org.hszg.learner_implementations.neuronal_network.modelToClassification
import org.hszg.training.TrainingData

class NeuronalNetwork : Learner {
    private val perceptron: List<Perceptron> = listOf(Perceptron(), Perceptron(), Perceptron())


    override fun classify(featureVector: List<Double>): SignClassification {
        val res = perceptron.map { it.classify(featureVector) }
        if (res.size != 3) {
            throw IllegalArgumentException("The perceptron should have 3 outputs")
        }

        return modelToClassification(Model(res[0], res[1], res[2]))
    }

    override fun learn(trainingData: Set<TrainingData>) {
        repeat(10000) {
            for (data in trainingData) {
                val modelAsList = classificationToModel(data.classification).toList()
                for (i in perceptron.indices) {
                    val perceptronOutput = modelAsList[i]
                    perceptron[i].learn(data.featureVector, perceptronOutput)
                }
            }
        }
    }

}