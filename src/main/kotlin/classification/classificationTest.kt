package org.hszg.classification

import org.hszg.LoadedSign
import org.hszg.SignClassification
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class classificationTest{

    private lateinit var sign: LoadedSign
    private lateinit var classification: SignClassification

    fun setTestData(sign: LoadedSign, classification: SignClassification){
        this.sign = sign
        this.classification = classification
    }

    @Test
    fun testSignClassification(){
        val expectedClassification = sign.classification
        val actualClassification = classification
        assertEquals(expectedClassification, actualClassification, "The classification should match the expected value.")
    }
}