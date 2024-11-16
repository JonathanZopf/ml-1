package org.hszg.sign_analyzer.color_analyzer

class WhiteCenterAnalyzingResult (private val xRatio: Double, private val yRatio: Double) {
    init {
        require(xRatio in 0.0..1.0) { "xRatio must be between 0 and 1" }
        require(yRatio in 0.0..1.0) { "yRatio must be between 0 and 1" }
    }

    fun getXRatio(): Double = xRatio
    fun getYRatio(): Double = yRatio

    override fun toString(): String {
        var corner = ""
        if (xRatio < 0.5 && yRatio < 0.5) {
            corner = "top left"
        }
        if (xRatio >= 0.5 && yRatio < 0.5) {
            corner = "top right"
        }
        if (xRatio < 0.5 && yRatio >= 0.5) {
            corner = "bottom left"
        }
        if (xRatio >= 0.5 && yRatio >= 0.5) {
            corner = "bottom right"
        }
        return "x=$xRatio, y=$yRatio, corner=$corner"
    }
}