package org.hszg.sign_properties

import com.google.gson.Gson
import java.awt.Color

/** Properties of a sign.
 * @param colors List of colors of the sign. [SignColor] contains several properties of a color like the share of the color on the sign.
 * @param colorChangesHorizontal Number of color changes in horizontal direction.
 * @param colorChangesVertical Number of color changes in vertical direction.
 * */
class SignProperties(private val colors: List<SignColor>, private val colorChangesHorizontal: Int, private val colorChangesVertical: Int) {
    init {
        require(colors.isNotEmpty()) { "List of colors must not be empty" }
        require(colorChangesHorizontal >= 0) { "Number of color changes in horizontal direction must be greater or equal to 0" }
        require(colorChangesVertical >= 0) { "Number of color changes in vertical direction must be greater or equal to 0" }
    }

    fun toJson() : String {
        return Gson().toJson(this).toString()
    }

    fun getColors(): List<SignColor> = colors
    fun getColorChangesHorizontal(): Int = colorChangesHorizontal
    fun getColorChangesVertical(): Int = colorChangesVertical
}