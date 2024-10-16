package org.hszg

import com.google.gson.Gson
import nu.pattern.OpenCV
import org.hszg.sign_analyzer.SignAnalysisException
import org.hszg.sign_analyzer.analyzeSign

fun main() {
    println("Starting sign analysis")
    OpenCV.loadShared()
    val signs = loadSigns()
    signs.forEach { sign ->
        try {
           val signProperties = analyzeSign(sign)
            val colors = signProperties.getColors().map { it.getShareOnSign() }.toList()
        val colorsLeft = signProperties.getColorsLeft().map { it.getShareOnSign() }.toList()
        val colorsRight = signProperties.getColorsRight().map { it.getShareOnSign() }.toList()
        val shape = signProperties.getShape().ordinal

        // Print the data
        val gson = Gson()
        val finalFeatures = listOf(colors, colorsLeft, colorsRight, listOf(shape.toDouble())).flatten()
        val featureJson = gson.toJson(finalFeatures)
        println(featureJson)
        } catch (e: SignAnalysisException) {
            println(e.message)
            println("An error occurred while analyzing sign "+ sign.path)
        }
    }
}