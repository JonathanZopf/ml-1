package org.hszg.classification

import org.hszg.LoadedSign
import org.hszg.SignClassification

class classificationTest{

    fun testSignClassification(sign: LoadedSign, classification: SignClassification): Boolean{
        val expectedClassification = sign.classification
        val actualClassification = classification
        if (expectedClassification == actualClassification){
            println("Sign ${sign.path} was classified correctly")
            return true
        } else {
            println("Sign ${sign.path} was classified wrongly as $classification, while the actual type is ${sign.classification}")
            return false
        }
    }


    fun evaluateSignClassification(signs: List<LoadedSign>, classifications: List<SignClassification>){
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
}