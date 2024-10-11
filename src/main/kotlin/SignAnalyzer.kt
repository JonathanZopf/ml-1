package org.hszg

import org.hszg.sign_cropper.cropSignWithTransparency
import org.hszg.sign_properties.SignColor
import org.hszg.sign_properties.SignProperties
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

fun analyzeSign(sign: Mat) : SignProperties {
    val croppedSign = cropSignWithTransparency(sign)
    throw NotImplementedError("Not implemented yet")
}

private fun analyzeColors(croppedSign: Mat): List<SignColor> {

}

