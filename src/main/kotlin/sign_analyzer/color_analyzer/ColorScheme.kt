package org.hszg.sign_analyzer.color_analyzer

import java.awt.Color
import kotlin.math.pow

enum class ApproximatedColor {
    WHITE, BLACK, RED, YELLOW, BLUE;

    fun toRGB(): DoubleArray {
        return when (this) {
            WHITE -> doubleArrayOf(255.0, 255.0, 255.0)
            BLACK -> doubleArrayOf(0.0, 0.0, 0.0)
            RED -> doubleArrayOf(255.0, 0.0, 0.0)
            YELLOW -> doubleArrayOf(255.0, 255.0, 0.0)
            BLUE -> doubleArrayOf(0.0, 0.0, 255.0)
        }
    }
}

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

    fun calculateDistanceToColorScheme(pixels: List<Color>): Long {
        return pixels.sumOf {
            findBestMatchingColorWithDistance(it).second.toLong()
        }
    }

    fun findBestMatchingColor(pixel: Color): ApproximatedColor {
       return findBestMatchingColorWithDistance(pixel).first
    }

    private fun findBestMatchingColorWithDistance(pixel: Color): Pair<ApproximatedColor, Int> {
        val closestColor = colors.minByOrNull { (_, color) ->
            calculateDistanceToColor(pixel, color)
        } ?: throw IllegalArgumentException("No colors in the scheme")

        return closestColor.key to calculateDistanceToColor(pixel, closestColor.value)
    }

    private fun calculateDistanceToColor(pixel: Color, colorToCompare: Color): Int {
        return ((pixel.red - colorToCompare.red).toDouble().pow(2.0) +
                (pixel.green - colorToCompare.green).toDouble().pow(2.0) +
                (pixel.blue - colorToCompare.blue).toDouble().pow(2.0)).toInt()
    }
}
