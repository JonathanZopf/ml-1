package org.hszg.neuronal_network

class Perceptron {
    private var weights = ArrayList<Double>()
    private val learningRate = 0.01

    init {
        val weightsSize = 16 +1;
        for (i in 0 until weightsSize) {
            weights.add(Math.random())
        }
    }

    fun classify(featureVector: List<Double>) : Boolean {
        val sum = featureVector.zip(weights).sumOf { (x, w) -> x * w }
        return sum >= 0
    }

    fun learn(featureVector: List<Double>, expectedOutput: Boolean) {
        val featureVectorPlusOne = featureVector.toMutableList().apply { add(1.0) }
        val actualOutput = classify(featureVectorPlusOne)
        val error = if (expectedOutput) 1 else -1 - if (actualOutput) 1 else -1

        if (error != 0) {
            for (i in weights.indices) {
                weights[i] += learningRate * error * featureVectorPlusOne[i]
            }
        }
    }
}