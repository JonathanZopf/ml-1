package org.hszg.training

import jdk.incubator.vector.Vector
import org.hszg.SignClassification

data class TrainingData (val classification: SignClassification, val featureVector: List<Double>)