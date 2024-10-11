package org.hszg

import nu.pattern.OpenCV
import org.hszg.sign_analyzer.analyzeSign
import org.opencv.imgcodecs.Imgcodecs

fun main() {
    println("Hello World!")
    OpenCV.loadShared()
    val sign = Imgcodecs.imread("/Users/jonathan/Downloads/X-10Y-10.bmp")
    val signProperties = analyzeSign(sign)
    println(signProperties.toJson())
}