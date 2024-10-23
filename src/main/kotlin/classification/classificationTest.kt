package org.hszg.classification

import org.hszg.LoadedSign
import org.hszg.SignClassification

class classificationTest{

    fun testSignClassification(sign: LoadedSign, classification: SignClassification): Boolean{
        val expectedClassification = sign.classification
        val actualClassification = classification
        if (expectedClassification == actualClassification){
            return true
        } else {
            return false
        }
    }
}