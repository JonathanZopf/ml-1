package org.hszg.classification

import org.hszg.LoadedSign
import org.hszg.SignClassification

class classificationTest{

    fun testSignClassification(sign: LoadedSign, classification: SignClassification){
        val expectedClassification = sign.classification
        val actualClassification = classification
        if (expectedClassification == actualClassification){
            println("Klassifikation wurde richtig erkannt")
        } else {
            println("Klassifikation wurde falsch erkannt")
        }
    }
}