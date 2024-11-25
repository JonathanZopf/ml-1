import org.hszg.sign_analyzer.color_analyzer.ApproximatedColorSign
import org.hszg.sign_analyzer.color_analyzer.ApproximatedColorSignItem
import org.hszg.sign_analyzer.color_analyzer.ColorScheme
import org.hszg.sign_properties.ApproximatedColor
import org.hszg.sign_properties.SignColor
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.awt.Color
import java.util.*
import kotlin.collections.HashMap

/**
 * Analyzes the colors of a cropped sign and returns a list of SignColor objects.
 * Uses the precomputed approximated colors to determine the share of each color on the sign.
 *
 * @param croppedSign The cropped sign to analyze.
 * @return A pair containing:
 * - A list of SignColor objects representing the colors on the sign and their share.
 * - An ApproximatedColorSign object representing each pixel's position and approximated color.
 * @throws IllegalArgumentException if the input image is empty.
 */
fun analyzeColors(croppedSign: Mat): Pair<List<SignColor>, ApproximatedColorSign> {
    if (croppedSign.empty()) {
        throw IllegalArgumentException("The input image is empty")
    }

    val approximatedColorSign = getSignWithApproximatedColor(croppedSign)
    val colors = getColors(approximatedColorSign)

    return Pair(colors, approximatedColorSign)
}

/**
 * Gets all non-transparent pixels of a sign with their row, column, and color.
 *
 * @param sign The sign to analyze.
 * @return A list of triples where each entry contains:
 * - Row index of the pixel.
 * - Column index of the pixel.
 * - Color of the pixel.
 */
private fun getAllPixelColorsOfSignWithRowAndColumn(sign: Mat): List<Triple<Int, Int, Color>> {
    val pixels = LinkedList<Triple<Int, Int, Color>>()
    for (row in 0 until sign.rows()) {
        for (col in 0 until sign.cols()) {
            val pixel = sign.get(row, col)
            if (pixel[3] > 0.0) { // Check for non-transparent pixel
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
 * Finds the best color scheme for a cropped sign by minimizing the distance
 * between the sign's colors and the scheme's predefined colors.
 *
 * @param croppedSign The cropped sign to analyze.
 * @return The best-matching color scheme for the sign.
 */
private fun findBestColorScheme(croppedSign: Mat): ColorScheme {
    val pixels = getAllPixelColorsOfSign(croppedSign)
    return ColorScheme.entries.toTypedArray()
        .minByOrNull { it.calculateDistanceToColorScheme(pixels) }!!
}

/**
 * Calculates the share of each approximated color on the sign using the provided ApproximatedColorSign.
 *
 * @param approximatedColorSign The sign's approximated color data, containing a list of pixels with their positions and colors.
 * @return A list of SignColor objects representing the approximated colors and their respective shares.
 */
private fun getColors(approximatedColorSign: ApproximatedColorSign): List<SignColor> {
    // Initialize a map to count occurrences of each approximated color
    val colorsCount = HashMap<ApproximatedColor, Int>()
    ApproximatedColor.entries.forEach { colorsCount[it] = 0 }

    // Count each approximated color from the provided pixel data
    for (pixel in approximatedColorSign.getPixels()) {
        val color = pixel.getColor()
        colorsCount[color] = colorsCount[color]!! + 1
    }

    val totalPixels = approximatedColorSign.getPixels().size
    val signColors = ArrayList<SignColor>()

    // Calculate the share for each approximated color
    colorsCount.forEach { (color, count) ->
        val share = count / totalPixels.toDouble()
        signColors.add(SignColor(color, share))
    }

    return signColors
}


/**
 * Approximates the color of each non-transparent pixel in the sign based on the best-matching color scheme.
 *
 * @param sign The sign to analyze.
 * @return An ApproximatedColorSign object containing the approximated color data for each pixel.
 */
fun getSignWithApproximatedColor(sign: Mat): ApproximatedColorSign {
    val colorScheme = findBestColorScheme(sign)
    val pixels = getAllPixelColorsOfSignWithRowAndColumn(sign)

    val approximatedColorSignItems = pixels.map { (row, col, color) ->
        val approximatedColor = colorScheme.findBestMatchingColor(color)
        ApproximatedColorSignItem(row, col, approximatedColor)
    }
    return ApproximatedColorSign(approximatedColorSignItems)
}
