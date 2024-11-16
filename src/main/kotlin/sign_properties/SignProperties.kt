package org.hszg.sign_properties

import com.google.gson.Gson
import org.hszg.sign_analyzer.color_analyzer.WhiteCenterAnalyzingResult
import org.hszg.sign_analyzer.shape_recognizer.SignShape
import java.awt.Color

/** Properties of a sign.
 * @param colors List of colors of the sign. [SignColor] contains several properties of a color like the share of the color on the sign.
 * */
class SignProperties(private val colors: List<SignColor>, private val shape: SignShape, private val whiteCenter: WhiteCenterAnalyzingResult) {
    init {
       require(colors.size == ApproximatedColor.entries.size) { "List of colors must contain all colors" }
    }

    fun toJson() : String {
        return Gson().toJson(this).toString()
    }

    fun getColors(): List<SignColor> = colors
    fun getShape(): SignShape = shape
    fun getWhiteCenter(): WhiteCenterAnalyzingResult = whiteCenter

    fun toFeatureVector() : List<Double> {
        return listOf(colors.map { it.getShareOnSign() }, listOf(whiteCenter.getXRatio(), whiteCenter.getYRatio()), listOf(shape.ordinal.toDouble())).flatten()
    }
}