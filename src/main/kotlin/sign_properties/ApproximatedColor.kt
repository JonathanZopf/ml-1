package org.hszg.sign_properties

import java.awt.Color
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Enum class for the approximated colors of the signs.
 * Approximated colors are used to simplify the color detection.
 */
enum class ApproximatedColor {
    WHITE, BLACK, RED, YELLOW, BLUE;

    /**
     * Get the RGB values of the approximated color.
     * @return An array of the RGB values.
     */
    fun getRGBAArray() : DoubleArray{
        return when (this) {
            WHITE -> doubleArrayOf(255.0, 255.0, 255.0, 255.0)
            BLACK -> doubleArrayOf(0.0, 0.0, 0.0, 255.0)
            RED -> doubleArrayOf(255.0, 0.0, 0.0, 255.0)
            YELLOW -> doubleArrayOf(255.0, 255.0, 0.0, 255.0)
            BLUE -> doubleArrayOf(0.0, 0.0, 255.0, 255.0)
        }
    }
}