package org.hszg.sign_analyzer

import org.hszg.sign_cropper.cropSignWithTransparency
import org.hszg.sign_properties.SignProperties
import org.opencv.core.Mat
import org.opencv.core.Scalar

fun analyzeSign(sign: Mat) : SignProperties {
    val croppedSign = cropSignWithTransparency(sign)
    analyzeColors(croppedSign)
    throw NotImplementedError()
}
