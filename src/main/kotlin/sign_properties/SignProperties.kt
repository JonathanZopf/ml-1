package org.hszg.sign_properties

import com.google.gson.Gson
import org.hszg.sign_analyzer.shape_recognizer.SignShape
import java.awt.Color

/** Properties of a sign.
 * @param colors List of colors of the sign. [SignColor] contains several properties of a color like the share of the color on the sign.
 * @param colorChangesHorizontal Number of color changes in horizontal direction.
 * @param colorChangesVertical Number of color changes in vertical direction.
 * */
class SignProperties(private val colors: List<SignColor>, private val shape: SignShape, private colorsLeft:
List<>) {
    init {
        require(colors.isNotEmpty()) { "List of colors must not be empty" }
//        require(colorChangesHorizontal >= 0) { "Number of color changes in horizontal direction must be greater or equal to 0" }
//        require(colorChangesVertical >= 0) { "Number of color changes in vertical direction must be greater or equal to 0" }
        require(colors.size == ApproximatedColor.entries.size) { "List of colors must contain all colors" }
    }

    fun toJson() : String {
        return Gson().toJson(this).toString()
    }

    fun getColors(): List<SignColor> = colors
    fun getShape(): SignShape = shape
//    fun getColorChangesHorizontal(): Int = colorChangesHorizontal
//    fun getColorChangesVertical(): Int = colorChangesVertical
}