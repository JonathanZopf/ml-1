package org.hszg

import nu.pattern.OpenCV
import org.hszg.sign_analyzer.analyzeSign

fun main() {
    println("Hello World!")
    OpenCV.loadLocally()
    val signs = loadImagesLazily("C:/Users/Praktikant/Downloads/Verkehrszeichen")
    signs.forEachIndexed() { index, sign ->
        val signProperties = analyzeSign(sign,
        "C:/Users/Praktikant/Downloads/Verkehrszeichen/processed/$index.jpg")

        // Read data from the SignProperties object
        val colors = signProperties.getColors().map { it.getShareOnSign() }
        val colorsLeft = signProperties.getColorsLeft().map { it.getShareOnSign() }
        val colorsRight = signProperties.getColorsRight().map { it.getShareOnSign() }
        val shape = signProperties.getShape().ordinal

        // Print the data



        println("Sign $index: ,Color Shares: $colors,colors Left: $colorsLeft, colors Right: $colorsRight, Shape:$shape")

    }
}