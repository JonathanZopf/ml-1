package org.hszg.sign_properties

import org.hszg.sign_analyzer.center_symbol_analyzer.CenterSymbolAnalyzerResult

/** Properties of a sign.
 * @param colors List of colors of the sign with their share on the total sign.
 * @param cornersCountNormalized The number of corners normalized to a value between 0 and 1.
 * @param centerSymbolAnalyzerResult The result of the center symbol analysis.
 * */
class SignProperties(private val colors: List<SignColor>, private val cornersCountNormalized: Double, private val centerSymbolAnalyzerResult: CenterSymbolAnalyzerResult) {
    init {
       require(colors.size == ApproximatedColor.entries.size) { "List of colors must contain all colors" }
         require(cornersCountNormalized in 0.0..1.0) { "Corners count must be between 0 and 1" }
    }

    fun toFeatureVector() : List<Double> {
        return listOf(colors.map { it.getShareOnSign() }, listOf(cornersCountNormalized), listOf(centerSymbolAnalyzerResult.getCenterSymbolColorAvg(), centerSymbolAnalyzerResult.getPercentagePixelsLeft(), centerSymbolAnalyzerResult.getPercentagePixelsRight())).flatten()
    }
}