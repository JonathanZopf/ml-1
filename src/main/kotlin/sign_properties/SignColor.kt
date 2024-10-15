package org.hszg.sign_properties

import java.awt.Color

/**
 * Represents a color on a sign.
 * @property approximatedColor The approximated color of the sign. Used to simplify the color detection.
 * @property shareOnTotalSign The share of the color on the total sign, excluding the background.
 * @property shareLeft The share of the color on the left side of the sign.
 * @property shareRight The share of the color on the right side of the sign.
 */
data class SignColor(private val approximatedColor: ApproximatedColor, private val shareOnTotalSign: Double, private val shareLeft: Double, private val shareRight: Double) {
    init {
        require(shareOnTotalSign in 0.0..1.0) { "Share must be between 0 and 1" }
        require(shareLeft + shareRight == 1.0) { "Share left and right must sum up to 1" }
        require(shareLeft >= 0) { "Share left must be greater or equal to 0" }
        require(shareRight >= 0) { "Share right must be greater or equal to 0" }
    }

    fun getApproximatedColor(): ApproximatedColor = approximatedColor
    fun getShareOnTotalSign(): Double = shareOnTotalSign
    fun getShareLeft(): Double = shareLeft
    fun getShareRight(): Double = shareRight
}
