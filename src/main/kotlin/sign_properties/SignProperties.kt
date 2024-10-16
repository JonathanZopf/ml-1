package org.hszg.sign_properties

import com.google.gson.Gson
import org.hszg.sign_analyzer.shape_recognizer.SignShape
import java.awt.Color

/** Properties of a sign.
 * @param colors List of colors of the sign. [SignColor] contains several properties of a color like the share of the color on the sign.
 * */
class SignProperties(private val colors: List<SignColor>, private val shape: SignShape, private val colorsLeft: List<SignColor>, private val colorsRight: List<SignColor>) {
    init {
       require(colors.size == ApproximatedColor.entries.size) { "List of colors must contain all colors" }
        require(colorsLeft.size == ApproximatedColor.entries.size) { "List of colors must contain all colors" }
        require(colorsRight.size == ApproximatedColor.entries.size) { "List of colors must contain all colors" }
    }

    fun toJson() : String {
        return Gson().toJson(this).toString()
    }

    fun getColors(): List<SignColor> = colors
    fun getShape(): SignShape = shape
    fun getColorsLeft(): List<SignColor> = colorsLeft
    fun getColorsRight(): List<SignColor> = colorsRight

    fun toFeatureVector() : List<Double> {
        return listOf(colors.map { it.getShareOnSign() }, colorsLeft.map { it.getShareOnSign() }, colorsRight.map { it.getShareOnSign() }, listOf(shape.ordinal.toDouble())).flatten()
    }
}