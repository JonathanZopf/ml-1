package org.hszg.sign_properties

import com.google.gson.Gson
import org.hszg.sign_analyzer.color_analyzer.WhiteCenterAnalyzingResult
import java.awt.Color

/** Properties of a sign.
 * @param colors List of colors of the sign. [SignColor] contains several properties of a color like the share of the color on the sign.
 * @param cornersCountNormalized The number of corners normalized to a value between 0 and 1.
 * @param whiteCenter The position of the white center of the sign.
 * */
class SignProperties(private val colors: List<SignColor>, private val cornersCountNormalized: Double, private val whiteCenter: WhiteCenterAnalyzingResult) {
    init {
       require(colors.size == ApproximatedColor.entries.size) { "List of colors must contain all colors" }
         require(cornersCountNormalized in 0.0..1.0) { "Corners count must be between 0 and 1" }
    }

    fun toFeatureVector() : List<Double> {
        return listOf(colors.map { it.getShareOnSign() }, listOf(cornersCountNormalized, whiteCenter.getXRatio(), whiteCenter.getYRatio())).flatten()
    }
}