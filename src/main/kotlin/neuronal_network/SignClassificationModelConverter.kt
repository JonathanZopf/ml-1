package org.hszg.neuronal_network

import org.hszg.SignClassification
class Model(val bit1: Boolean, val bit2: Boolean, val bit3: Boolean) {
    fun toList(): List<Boolean> {
        return listOf(bit1, bit2, bit3)
    }

    override fun toString(): String {
        return "Model($bit1, $bit2, $bit3)"
    }

    // equals and hashCode overrides are needed for the list operations
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is Model) return false

        return bit1 == other.bit1 && bit2 == other.bit2 && bit3 == other.bit3
    }

    override fun hashCode(): Int {
        var result = bit1.hashCode()
        result = 31 * result + bit2.hashCode()
        result = 31 * result + bit3.hashCode()
        return result
    }
}

private val classificationModelMap = mapOf(
    SignClassification.VORFAHRT_VON_RECHTS to Model(false, false, false),
    SignClassification.VORFAHRT_GEWAEHREN to Model(false, false, true),
    SignClassification.STOP to Model(false, true, false),
    SignClassification.FAHRTRICHTUNG_LINKS to Model(false, true, true),
    SignClassification.FAHRTRICHTUNG_RECHTS to Model(true, false, false),
    SignClassification.VORFAHRTSSTRASSE to Model(true, false, true),
    SignClassification.UNKNOWN to Model(true, true, false),
    SignClassification.UNKNOWN to Model(true, true, true)
)

/**
 * Function to convert SignClassification to Model
 * @param classification SignClassification to convert
 * @return Model corresponding to the SignClassification
 */
fun classificationToModel(classification: SignClassification): Model {
    return classificationModelMap[classification]!!
}

// Function to convert Model to SignClassification
fun modelToClassification(model: Model): SignClassification {
    try {
        return classificationModelMap.entries.first { it.value == model }.key
    } catch (ex: NoSuchElementException) {
        throw IllegalArgumentException("No SignClassification found for model $model")
    }
}