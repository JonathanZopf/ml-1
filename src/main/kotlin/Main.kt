package org.hszg

import nu.pattern.OpenCV
import org.hszg.sign_analyzer.FeatureVector
import org.hszg.sign_analyzer.analyzeSign
import org.opencv.imgcodecs.Imgcodecs

fun main() {
    println("Hello World!")
    OpenCV.loadLocally()
    val signs = loadImagesLazily("C:/Users/Praktikant/Downloads/Verkehrszeichen")
    signs.forEachIndexed() { index, sign ->
        val signProperties = analyzeSign(sign,
        "C:/Users/Praktikant/Downloads/Verkehrszeichen/processed/$index.jpg")

        // Read data from the SignProperties object
        val colors = signProperties.getColors().map { it.getShareOnSign() }
        val shape = signProperties.getShape().ordinal



        println("Sign $index: ,Color Shares: $colors, Shape: $shape")
    }
}