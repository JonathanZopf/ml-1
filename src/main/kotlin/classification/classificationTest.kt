package org.hszg.classification

import org.hszg.LoadedSign
import org.hszg.SignClassification

class classificationTest{

    fun testSignClassification(sign: LoadedSign, classification: SignClassification): Int{
        val expectedClassification = sign.classification
        val actualClassification = classification
        if (expectedClassification == actualClassification){
            return 1
        } else {
            return 0
        }
    }
}