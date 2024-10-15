package org.hszg

import nu.pattern.OpenCV
import org.hszg.sign_analyzer.analyzeSign
import org.opencv.imgcodecs.Imgcodecs

fun main() {
    println("Hello World!")
    OpenCV.loadShared()
    val signs = loadImagesLazily("/Users/jonathan/Downloads/Verkehrszeichen")
    signs.forEachIndexed() { index, sign ->
        analyzeSign(sign, "/Users/jonathan/Downloads/Verkehrszeichen/processed/$index.jpg")
    }
}