package org.hszg

import nu.pattern.OpenCV
import org.hszg.sign_analyzer.SignAnalysisException
import org.hszg.sign_analyzer.analyzeSign
import org.opencv.imgcodecs.Imgcodecs

fun main() {
    println("Starting sign analysis")
    OpenCV.loadShared()
    val signs = loadSigns()
    signs.forEachIndexed() { index, sign ->
        try {
            analyzeSign(sign.image, "/Users/jonathan/Downloads/Verkehrszeichen/processed/$index.jpg")
        } catch (e: SignAnalysisException) {
            println(e.message)
            println("An error occurred while analyzing sign "+ sign.path)
        }
    }
}