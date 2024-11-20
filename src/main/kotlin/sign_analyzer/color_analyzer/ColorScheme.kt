package org.hszg.sign_analyzer.color_analyzer

import org.hszg.sign_properties.ApproximatedColor
import java.awt.Color
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Enum class for the color schemes of the signs. Used to simplify the color detection.
 * A color scheme consists of approximated colors and their RGB values.
 * Different color schemes exist to allow for color approximation in different lighting conditions.
 * @property colors A map of approximated colors and their RGB values.
 */
enum class ColorScheme(
    private val colors: Map<ApproximatedColor, Color>
) {
    VERY_DIM(
        mapOf(
            ApproximatedColor.WHITE to Color(101, 99, 99),
            ApproximatedColor.BLACK to Color(0, 0, 0),
            ApproximatedColor.RED to Color(35, 3, 13),
            ApproximatedColor.YELLOW to Color(92, 69, 24),
            ApproximatedColor.BLUE to Color(4, 21, 34)
        )
    ),
    DIM(
        mapOf(
            ApproximatedColor.WHITE to Color(205, 203, 201),
            ApproximatedColor.BLACK to Color(23, 22, 19),
            ApproximatedColor.RED to Color(99, 35, 27),
            ApproximatedColor.YELLOW to Color(196, 155, 48),
            ApproximatedColor.BLUE to Color(35, 62, 92)
        )
    ),
    NEUTRAL(
        mapOf(
            ApproximatedColor.WHITE to Color(255, 255, 255),
            ApproximatedColor.BLACK to Color(43, 43, 40),
            ApproximatedColor.RED to Color(203, 67, 45),
            ApproximatedColor.YELLOW to Color(249, 226, 73),
            ApproximatedColor.BLUE to Color(81, 132, 187)
        )
    ),
    BRIGHT(
        mapOf(
            ApproximatedColor.WHITE to Color(255, 255, 255),
            ApproximatedColor.BLACK to Color(75, 76, 79),
            ApproximatedColor.RED to Color(214, 127, 114),
            ApproximatedColor.YELLOW to Color(255, 253, 161),
            ApproximatedColor.BLUE to Color(131, 185, 236)
        )
    ),
    VERY_BRIGHT(
        mapOf(
            ApproximatedColor.WHITE to Color(255, 255, 255),
            ApproximatedColor.BLACK to Color(117, 122, 128),
            ApproximatedColor.RED to Color(242, 182, 174),
            ApproximatedColor.YELLOW to Color(255, 254, 208),
            ApproximatedColor.BLUE to Color(187, 222, 252)
        )
    );

    /**
     * Calculates the distance of pixels of a sign to the color scheme.
     * The distance is the sum of the squared differences of the RGB values of the pixel and the approximated color.
     * Long to avoid overflow.
     * @param pixels The pixels of the sign.
     * @return The distance of the pixels to the color scheme.
     */
    fun calculateDistanceToColorScheme(pixels: List<Color>): Long {
        return pixels.sumOf {
            findBestMatchingColorWithDistance(it).second.toLong()
        }
    }

    /**
     * Finds the best matching color of a pixel in the color scheme.
     * The best matching color is the one with the smallest distance to the pixel.
     * @param pixel The pixel to find the best matching color for.
     * @return The best matching color.
     */
    fun findBestMatchingColor(pixel: Color): ApproximatedColor {
       return findBestMatchingColorWithDistance(pixel).first
    }

    /**
     * Finds the best matching color of a pixel in the color scheme and also returns the distance to the pixel.
     * The best matching color is the one with the smallest distance to the pixel.
     * @param pixel The pixel to find the best matching color for.
     * @return The best matching color and the distance to the pixel.
     */
    private fun findBestMatchingColorWithDistance(pixel: Color): Pair<ApproximatedColor, Int> {
        val closestColor = colors.minByOrNull { (_, color) ->
            calculateDistanceToColor(pixel, color)
        }!!

        return closestColor.key to calculateDistanceToColor(pixel, closestColor.value)
    }

    /**
     * Calculates the distance of a pixel to a color.
     * The distance is the Euclidean distance of the RGB values of the pixel and the color.
     * @param pixel The pixel to calculate the distance for.
     * @param colorToCompare The color to compare the pixel to.
     * @return The distance of the pixel to the color.
     */
    private fun calculateDistanceToColor(pixel: Color, colorToCompare: Color): Int {
        return sqrt((pixel.red - colorToCompare.red).toDouble().pow(2.0) +
                (pixel.green - colorToCompare.green).toDouble().pow(2.0) +
                (pixel.blue - colorToCompare.blue).toDouble().pow(2.0)).toInt()
    }
}
