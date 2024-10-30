package org.hszg.learner_implementations

import org.hszg.Learner
import org.hszg.SignClassification
import org.hszg.training.TrainingData
import kotlin.math.log2

/**
 * A decision tree learner.
 * The complexity of the algorithm for classification is O(n) with n the number of features.
 */
class DecisionTree : Learner {
    private var successor: Map<Number, DecisionTree> = mapOf()
    private var classification: SignClassification? = null
    private var depth: Int = 0
    private var pattern: List<Double>? = null
    private var examples: MutableList<TrainingData> = mutableListOf()

    override fun learn(trainingData: Set<TrainingData>) {
        trainingData.forEach { include(it)}
    }

    override fun classify(featureVector: List<Double>): SignClassification {
        if (classification != null) return classification!!

        val bestAttribute = findBestAttribute(examples.toSet())
        val attributeValue = featureVector[bestAttribute]
        val subtree = successor[attributeValue.toInt()] ?: return SignClassification.UNKNOWN
        return subtree.classify(featureVector)
    }

    private fun include(newExample: TrainingData) {
        if (classification != null){
            if (classification == newExample.classification) {
                examples.add(newExample)
                return
            }else{
                //Split the node
                val bestAttribute = findBestAttribute(examples.toSet() + newExample)
                if (bestAttribute == -1) return

                val partitions = (examples + newExample).groupBy { it.featureVector[bestAttribute] }
                successor = partitions.mapValues { (_,subset) ->
                    DecisionTree().apply {
                        depth = this@DecisionTree.depth + 1
                        learn(subset.toSet())
                    }
                }
                classification = null
                pattern = null
                examples.clear()
            }
        }else{
            if (successor.isEmpty()){
                classification = newExample.classification
                pattern = newExample.featureVector
                examples.add(newExample)
            }else{
                val bestAttribute = findBestAttribute(examples.toSet() + newExample)
                val attributeValue = newExample.featureVector[bestAttribute]
                successor[attributeValue.toInt()]?.include(newExample)
            }
        }
    }

    private fun findBestAttribute(trainingData: Set<TrainingData>): Int {
        val numAttributes = trainingData.first().featureVector.size
        var bestAttribute = -1
        var bestGain = Double.NEGATIVE_INFINITY

        for (attribute in 0 until numAttributes){
            val gain = informationGain(trainingData, attribute)
            if (gain > bestGain){
                bestGain = gain
                bestAttribute = attribute
            }
        }
        return bestAttribute
    }

    private fun informationGain(trainingData: Set<TrainingData>, attribute: Int): Double{
        val totalEntropy = entropy(trainingData)
        val partitions = trainingData.groupBy { it.featureVector[attribute] }
        val weightedEntropy = partitions.values.sumOf { subset ->
            (subset.size.toDouble() / trainingData.size) * entropy(subset.toSet())
        }
        return totalEntropy - weightedEntropy
    }

    private fun entropy(data: Set<TrainingData>): Double {
        val total = data.size.toDouble()
        val counts = data.groupingBy { it.classification }.eachCount()
        return counts.values.sumOf { count ->
            val p = count / total
            -p * log2(p)
        }
    }
}