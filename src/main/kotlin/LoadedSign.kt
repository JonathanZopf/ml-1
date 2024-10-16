package org.hszg

import org.opencv.core.Mat

data class LoadedSign (val path: String, val classification: SignClassification, val image: Mat)