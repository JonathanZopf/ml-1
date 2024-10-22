package org.hszg.classification

import org.hszg.SignClassification

data class Neighbor (
    val classification: SignClassification,
    val distance: Double
)
