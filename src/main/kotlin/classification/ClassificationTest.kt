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

    fun getConfidenceInterval(){
        //TODO:
    }