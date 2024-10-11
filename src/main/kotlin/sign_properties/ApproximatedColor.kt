package org.hszg.sign_properties

import java.awt.Color
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Enum class for the approximated colors of the signs.
 * Approximated colors are used to simplify the color detection.
 * @param standardColor The standard color of the approximated color. Used to calculate the distance of a pixel's color to the standard color.
 */
enum class ApproximatedColor(private val standardColor: Color) {
    RED(Color(216, 113, 93)), YELLOW(Color(249, 169, 0)), GREEN(Color(35, 127, 82)), BLUE(Color(142, 186, 232)), WHITE(Color.WHITE), BLACK(Color.BLACK);

    /**
     * Calculate the distance of a pixel's color to the standard color.
     * @param colorToCompare The color to compare with the standard color of the approximation.
     */
    fun calculateDistanceFromColor(colorToCompare: Color) : Double {
        return sqrt(
            (colorToCompare.red - standardColor.red).toDouble().pow(2.0) +
                    (colorToCompare.green - standardColor.green).toDouble().pow(2.0) +
                    (colorToCompare.blue - standardColor.blue).toDouble().pow(2.0)
        )
    }
}