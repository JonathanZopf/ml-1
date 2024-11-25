package org.hszg.sign_analyzer.center_symbol_analyzer

import kotlin.math.roundToInt

/**
 * Represents the result of analyzing the center symbol of an image or region.
 * Stores the average color value of the center symbol and the percentages of
 * certain pixel values on the left and right sides of the region.
 *
 * @property centerSymbolColorAvg the average color value of the center symbol (0.0 to 1.0).
 * @property percentagePixelsLeft the percentage of relevant pixels on the left side (0.0 to 1.0).
 * @property percentagePixelsRight the percentage of relevant pixels on the right side (0.0 to 1.0).
 * @throws IllegalArgumentException if any of the properties are outside the range 0.0 to 1.0.
 */
class CenterSymbolAnalyzerResult(
    private val centerSymbolColorAvg: Double,
    private val percentagePixelsLeft: Double,
    private val percentagePixelsRight: Double
) {
    init {
        // Validate that all properties are within the range [0.0, 1.0].
        require(centerSymbolColorAvg in 0.0..1.0) {
            "Center symbol color average must be between 0 and 1"
        }
        require(percentagePixelsLeft in 0.0..1.0) {
            "Percentage of pixels left must be between 0 and 1"
        }
        require(percentagePixelsRight in 0.0..1.0) {
            "Percentage of pixels right must be between 0 and 1"
        }
    }

    /**
     * Converts the analysis result into a human-readable string representation.
     *
     * @return a string describing the center symbol color average and the percentages
     * of pixels on the left and right sides.
     */
    override fun toString(): String {
        return "CS color: $centerSymbolColorAvg, " +
                "CS left: ${(percentagePixelsLeft * 100).roundToInt()}%, " +
                "CS right: ${(percentagePixelsRight * 100).roundToInt()}%"
    }

    /**
     * Gets the average color value of the center symbol.
     *
     * @return a double value between 0.0 and 1.0 representing the average color.
     */
    fun getCenterSymbolColorAvg(): Double = centerSymbolColorAvg

    /**
     * Gets the percentage of relevant pixels on the left side of the region.
     *
     * @return a double value between 0.0 and 1.0 representing the percentage of relevant pixels on the left side.
     */
    fun getPercentagePixelsLeft(): Double = percentagePixelsLeft

    /**
     * Gets the percentage of relevant pixels on the right side of the region.
     *
     * @return a double value between 0.0 and 1.0 representing the percentage of relevant pixels on the right side.
     */
    fun getPercentagePixelsRight(): Double = percentagePixelsRight
}
