package org.hszg.sign_properties

import java.awt.Color

/**
 * Represents a color on a sign.
 * @property approximatedColor The approximated color of the sign. Used to simplify the color detection.
 * @property shareOnSign The share of the color on the total sign, excluding the background.
 */
data class SignColor(private val approximatedColor: ApproximatedColor, private val shareOnSign: Double) {
    init {
      require(shareOnSign in 0.0..1.0) { "Share must be between 0 and 1" }
    }

    fun getApproximatedColor(): ApproximatedColor = approximatedColor
    fun getShareOnSign(): Double = shareOnSign
}
