package org.hszg

import com.google.gson.Gson
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
        val colors = signProperties.getColors().map { it.getShareOnSign() }.toList()
        val colorsLeft = signProperties.getColorsLeft().map { it.getShareOnSign() }.toList()
        val colorsRight = signProperties.getColorsRight().map { it.getShareOnSign() }.toList()
        val shape = signProperties.getShape().ordinal

        // Print the data
        val gson = Gson()
        val finalFeatures = listOf(colors, colorsLeft, colorsRight, listOf(shape.toDouble())).flatten()
        val featureJson = gson.toJson(finalFeatures)
        println(featureJson)
    }
}