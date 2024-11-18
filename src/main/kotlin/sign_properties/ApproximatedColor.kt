package org.hszg.sign_properties

import java.awt.Color
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Enum class for the approximated colors of the signs.
 * Approximated colors are used to simplify the color detection.
 * @param standardColor The standard color of the approximated color. Used to calculate the distance of a pixel's color to the standard color.
 */
@Deprecated("This class is deprecated, remove in next commit")
enum class ApproximatedColor(private val standardColor: Color) {
    RED(Color.red), YELLOW(Color.yellow), BLUE(Color.blue), WHITE(Color.white), BLACK(Color.black);

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