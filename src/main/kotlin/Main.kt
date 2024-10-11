package org.hszg

import nu.pattern.OpenCV
import org.hszg.sign_cropper.cropSignWithTransparency
import org.opencv.imgcodecs.Imgcodecs

fun main() {
    println("Hello World!")
    OpenCV.loadShared()
    val sign = Imgcodecs.imread("/Users/jonathan/Downloads/files/X-10Y-10.jpg")
    val signProperties = analyzeSign(sign)
    println(signProperties.toJson())
}