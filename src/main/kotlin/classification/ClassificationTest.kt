package org.hszg.classification

import org.hszg.SignClassification
import org.hszg.SignLoading.LoadableSign

    fun testSignClassification(sign: LoadableSign, classification: SignClassification): Boolean{
        val expectedClassification = sign.getClassification()
        val actualClassification = classification
        if (expectedClassification == actualClassification){
            println("Sign ${sign.getAbsolutePath()} was classified correctly")
            return true
        } else {
            println("Sign ${sign.getAbsolutePath()} was classified wrongly as $classification, while the actual type is ${sign.getClassification()}")
            return false
        }
    }


    fun evaluateSignClassification(signs: List<LoadableSign>, classifications: List<SignClassification>){
        var correctlyClassifiedSigns = 0
        for (i in 0 until signs.size){
            if (testSignClassification(signs[i], classifications[i])){
                correctlyClassifiedSigns++
            }
        }
        println("Successfully classified $correctlyClassifiedSigns out of ${signs.size} signs (${
            (correctlyClassifiedSigns.toDouble() / signs.size.toDouble()) * 100}%)c")
    }

    fun getConfidenceInterval(data: List<Double>): List<Double>{
        val mean = data.average()
        val standardDeviation = Math.sqrt(data.map { it ->
            Math.pow((it - mean), 2.0)
        }.sum() / data.size)
        val standardError = standardDeviation / Math.sqrt(data.size.toDouble())
        val z = 1.96

        val confindenceInterval = z * standardError
        val lowerBound = mean - confindenceInterval
        val upperBound = mean + confindenceInterval

        return listOf(confindenceInterval, lowerBound, upperBound)
    }