import org.hszg.sign_analyzer.color_analyzer.WhiteCenterAnalyzingResult
import org.hszg.sign_properties.ApproximatedColor
import org.hszg.sign_properties.SignColor
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.CLAHE
import org.opencv.imgproc.Imgproc
import java.awt.Color
import java.awt.Image
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Analyzes the colors of a cropped sign and returns a list of SignColor objects.
 * Finds all colors on the sign and approximates them to the closest color from the ApproximatedColor enum.
 * Calculates the share of each approximated color on the sign as well as their share on the left and right side of the sign.
 * @param croppedSign The cropped sign to analyze.
 * @return A list of SignColor objects representing the colors on the sign.
 */
fun analyzeColors(croppedSign: Mat) : Pair<List<SignColor>, WhiteCenterAnalyzingResult>{
    if (croppedSign.empty()){
        throw IllegalArgumentException("The input image is empty")
    }

    val nonTransparentPixels = getNonTransparentPixels(croppedSign)

    val pixelsOfColor = getPixelsOfColor(croppedSign)

    return Pair(convertToSignColorList(pixelsOfColor, nonTransparentPixels), getWhiteCenter(croppedSign))
}

/**
 * Counts the non-transparent pixels in a cropped sign.
 * @param croppedSign The cropped sign to analyze.
 * @return The number of non-transparent pixels in the sign.
 */
fun getNonTransparentPixels(croppedSign: Mat): Int {
    var nonTransparentPixels = 0
    for (row in 0 until croppedSign.rows()) {
        for (col in 0 until croppedSign.cols()) {
            val pixel = croppedSign.get(row, col)
            if (pixel[3] > 0) {
                nonTransparentPixels++
            }
        }
    }
    return nonTransparentPixels
}

/**
 * Analyzes the colors of a sign (or a part) and returns a list of approximated colors with their pixel count on the sign.
 * Finds all colors on the sign and approximates them to the closest color from the ApproximatedColor enum.
 * @param croppedSign The cropped sign to analyze.
 * @return A list of pairs, each containing an approximated color and its pixel count on the sign.
 */
private fun getPixelsOfColor(croppedSign: Mat) : List<Pair<ApproximatedColor, Int>> {
    // Count the occurrences of each color and save the count of pixels in a hashmap
    val counted = HashMap<ApproximatedColor, Int>()
    for (approximatedColor in ApproximatedColor.entries) {
        counted[approximatedColor] = 0
    }

    for (row in 0 until croppedSign.rows()) {
        for (col in 0 until croppedSign.cols()) {
            val pixel = croppedSign.get(row, col)
            if (pixel[3] > 0) {
                val color = getApproximatedColor(Color(pixel[2].toInt(), pixel[1].toInt(), pixel[0].toInt()))
                counted[color] = counted.get(color)!! + 1
            }
        }
    }
    return counted.toList()
}

/**
 * Analyzes the colors of a cropped sign and returns the normalized center of the white pixels.
 * @param croppedSign The cropped sign to analyze.
 * @return The normalized center of the white pixels.
 */
private fun getWhiteCenter(croppedSign: Mat): WhiteCenterAnalyzingResult {
    val whitePixels = ArrayList<Pair<Int, Int>>()
    for (row in 0 until croppedSign.rows()) {
        for (col in 0 until croppedSign.cols()) {
            val pixel = croppedSign.get(row, col)
            if (pixel[3] > 0) {
                if (getApproximatedColor(Color(pixel[2].toInt(), pixel[1].toInt(), pixel[0].toInt())) == ApproximatedColor.WHITE) {
                    whitePixels.add(Pair(row, col))
                }
            }
        }
    }



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
 * Approximates a color to the closest color from the [ApproximatedColor] enum.
 * @param color The color to approximate.
 * @return The approximated color.
 */
private fun getApproximatedColor(color: Color) : ApproximatedColor {
    // First check if the color is a shade of gray and return black or white if it is
    // For this, check if the deviation of the color is small
    val numbers = listOf(color.red.toDouble(), color.green.toDouble(), color.blue.toDouble())
    val mean = numbers.average()
    val variance = numbers.map { (it - mean).pow(2) }.average()
    val deviation = sqrt(variance)
    if (deviation < 3) {
        // Check the mean to determine if the color is black or white
        if (mean < 100) {
            return ApproximatedColor.BLACK
        } else {
            return ApproximatedColor.WHITE
        }
    }
    // If the color is not black or white, approximate it to the closest color
    var bestMatch = Pair(ApproximatedColor.RED, Double.MAX_VALUE)
    for (approximatedColor in ApproximatedColor.entries) {
        if (approximatedColor == ApproximatedColor.BLACK || approximatedColor == ApproximatedColor.WHITE) {
            continue
        }
        val distance = approximatedColor.calculateDistanceFromColor(color)
        if (distance < bestMatch.second) {
            bestMatch = Pair(approximatedColor, distance)
        }
    }
    return bestMatch.first
}

/**
 * Converts a list of approximated colors with their pixel count to a list of SignColor objects.
 * Necessary because the share of each color on the sign needs to be calculated from the pixel count.
 * @param pixelsOfColor The list of approximated colors with their pixel count.
 * @param nonTransparentPixels The number of non-transparent pixels in the sign.
 */

private fun convertToSignColorList(pixelsOfColor: List<Pair<ApproximatedColor, Int>>, nonTransparentPixels: Int): List<SignColor> {
    return pixelsOfColor.map { (color, count) ->
        val share = (count.toDouble() / nonTransparentPixels)
        if (share.isNaN()) {
            SignColor(color, 0.0)
        } else {
            SignColor(color, share)
        }
    }
}