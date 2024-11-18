import org.hszg.sign_analyzer.color_analyzer.ColorScheme
import org.hszg.sign_analyzer.color_analyzer.WhiteCenterAnalyzingResult
import org.hszg.sign_properties.ApproximatedColor
import org.hszg.sign_properties.SignColor
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.CLAHE
import org.opencv.imgproc.Imgproc
import java.awt.Color
import java.awt.Image
import java.io.File
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Analyzes the colors of a cropped sign and returns a list of SignColor objects.
 * Finds all colors on the sign and approximates them to the closest color from the ApproximatedColor enum.
 * @param croppedSign The cropped sign to analyze.
 * @return A list of SignColor objects representing the colors on the sign.
 */
fun analyzeColors(croppedSign: Mat) : Pair<List<SignColor>, WhiteCenterAnalyzingResult>{
    if (croppedSign.empty()){
        throw IllegalArgumentException("The input image is empty")
    }

    val colorScheme = findBestColorScheme(croppedSign)
    val colors = getColors(croppedSign, colorScheme)
    val whiteCenter = getWhiteCenter(croppedSign, colorScheme)

    return Pair(colors, whiteCenter)
}

/**
 * Analyzes the colors of a cropped sign and returns the normalized center of the white pixels.
 * @param croppedSign The cropped sign to analyze.
 * @return The normalized center of the white pixels.
 */
private fun getWhiteCenter(croppedSign: Mat, colorScheme: ColorScheme): WhiteCenterAnalyzingResult {
    val whitePixels = getAllPixelColorsOfSignWithRowAndColumn(croppedSign).filter { colorScheme.findBestMatchingColor(it.third) == ApproximatedColor.WHITE }
        .map { Pair(it.first, it.second) }

   val normalizedPixels = whitePixels.map { (row, col) ->
       Pair( row / croppedSign.rows().toDouble(), col / croppedSign.cols().toDouble())
    }

    // Normalize the values so that the ratio is between 0 and 1
    val xNormalized = normalizedPixels.map { it.second }.average()
    val yNormalized = normalizedPixels.map { it.first }.average()

    if (xNormalized.isNaN() || yNormalized.isNaN()) {
        return WhiteCenterAnalyzingResult(0.5, 0.5)
    }

    return WhiteCenterAnalyzingResult(xNormalized, yNormalized)
}


/**
 * Gets all non-transparent pixels of a sign with its color and the row and column of the pixel.
 * @param sign The sign to analyze.
 * @return A list of all non-transparent pixels with their row, column and color.
 */
private fun  getAllPixelColorsOfSignWithRowAndColumn(sign: Mat) : List<Triple<Int, Int, Color>> {
    val pixels = LinkedList<Triple<Int, Int, Color>>()
    for (row in 0 until sign.rows()) {
        for (col in 0 until sign.cols()) {
            val pixel = sign.get(row, col)
            if (pixel[3] > 0.0) {
                pixels.add(Triple(row, col, Color(pixel[0].toInt(), pixel[1].toInt(), pixel[2].toInt())))
            }

        }
    }
    return pixels
}

/**
 * Gets all non-transparent pixels of a sign with its color.
 * @param sign The sign to analyze.
 * @return A list of all non-transparent pixels with their color.
 */
private fun getAllPixelColorsOfSign(sign: Mat) : List<Color> {
    return getAllPixelColorsOfSignWithRowAndColumn(sign).map { it.third }
}

/**
 * Finds the best color scheme for a cropped sign. The best color scheme is the one that minimizes the distance to the colors of the sign.
 * @param croppedSign The cropped sign to analyze.
 * @return The best color scheme for the sign.
 */
private fun findBestColorScheme(croppedSign: Mat) : ColorScheme {
    val pixels = getAllPixelColorsOfSign(croppedSign)
    val bestColorScheme = ColorScheme.entries.toTypedArray().minByOrNull { it.calculateDistanceToColorScheme(pixels) }!!
    return bestColorScheme
}

/**
 * Analyzes the colors of a cropped sign and returns a list of SignColor objects.
 * Finds all colors on the sign and approximates them to the closest color from the ApproximatedColor enum. The share of each color is calculated.
 * The color scheme is used to determine the best matching color. This is important in difficult lighting conditions.
 * @param croppedSign The cropped sign to analyze.
 * @param colorScheme The color scheme to use for approximating the colors.
 * @return A list of SignColor objects representing the colors on the sign.
 */
private fun getColors(croppedSign: Mat, colorScheme: ColorScheme) : List<SignColor> {
    val pixels = getAllPixelColorsOfSign(croppedSign)
    val colors = HashMap<ApproximatedColor, Int>()
    for (approximatedColor in ApproximatedColor.entries) {
        colors[approximatedColor] = 0
    }

    for (pixel in pixels) {
        val approximatedColor = colorScheme.findBestMatchingColor(pixel)
        colors[approximatedColor] = colors[approximatedColor]!! + 1
    }

    val totalPixels = pixels.size
    val signColors = ArrayList<SignColor>()
    for (approximatedColor in ApproximatedColor.entries) {
        val share = colors[approximatedColor]!! / totalPixels.toDouble()
        signColors.add(SignColor(approximatedColor, share))
    }

    return signColors
}