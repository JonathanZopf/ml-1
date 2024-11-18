import org.hszg.sign_analyzer.color_analyzer.ColorScheme
import org.hszg.sign_analyzer.color_analyzer.WhiteCenterAnalyzingResult
import org.hszg.sign_properties.ApproximatedColor
import org.hszg.sign_properties.SignColor
import org.hszg.sign_properties.SignColorNew
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
 * Calculates the share of each approximated color on the sign as well as their share on the left and right side of the sign.
 * @param croppedSign The cropped sign to analyze.
 * @return A list of SignColor objects representing the colors on the sign.
 */
fun analyzeColors(croppedSign: Mat) : Pair<List<SignColorNew>, WhiteCenterAnalyzingResult>{
    if (croppedSign.empty()){
        throw IllegalArgumentException("The input image is empty")
    }

    val currentTime = System.currentTimeMillis()
    val colorScheme = findBestColorScheme(croppedSign)
    println("Color Scheme determination took ${System.currentTimeMillis() - currentTime} ms and resulted in ${colorScheme.name}")


    val colors = getColors(croppedSign, colorScheme)
    saveDebugApproximatedColorImage(croppedSign, colorScheme)

    return Pair(colors, getWhiteCenter(croppedSign))
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


private fun getAllPixelColorsOfSign(sign: Mat) : List<Color> {
    val pixels = ArrayList<Color>()
    for (row in 0 until sign.rows()) {
        for (col in 0 until sign.cols()) {
            val pixel = sign.get(row, col)
            if (pixel[3] > 0.0) {
                pixels.add(Color(pixel[0].toInt(), pixel[1].toInt(), pixel[2].toInt()))
            }

        }
    }
    return pixels
}

private fun findBestColorScheme(croppedSign: Mat) : ColorScheme {
    val pixels = getAllPixelColorsOfSign(croppedSign)
    for (colorScheme in ColorScheme.entries) {
        println("Distance to ${colorScheme.name}: ${colorScheme.calculateDistanceToColorScheme(pixels) / pixels.size}")
    }
    val bestColorScheme = ColorScheme.entries.toTypedArray().minByOrNull { it.calculateDistanceToColorScheme(pixels) }
        ?: throw IllegalArgumentException("No color schemes available")
    return bestColorScheme
}

fun getColors(croppedSign: Mat, colorScheme: ColorScheme) : List<SignColorNew> {
    val pixels = getAllPixelColorsOfSign(croppedSign)
    val colors = HashMap<org.hszg.sign_analyzer.color_analyzer.ApproximatedColor, Int>()
    for (approximatedColor in org.hszg.sign_analyzer.color_analyzer.ApproximatedColor.entries) {
        colors[approximatedColor] = 0
    }

    for (pixel in pixels) {
        val approximatedColor = colorScheme.findBestMatchingColor(pixel)
        colors[approximatedColor] = colors[approximatedColor]!! + 1
    }

    val totalPixels = pixels.size
    val signColors = ArrayList<SignColorNew>()
    for (approximatedColor in org.hszg.sign_analyzer.color_analyzer.ApproximatedColor.entries) {
        val share = colors[approximatedColor]!! / totalPixels.toDouble()
        signColors.add(SignColorNew(approximatedColor, share))
    }

    return signColors
}

private fun saveDebugApproximatedColorImage(croppedSign: Mat, colorScheme: ColorScheme) {
    val debugImage = Mat(croppedSign.size(), croppedSign.type())

    for (row in 0 until croppedSign.rows()) {
        for (col in 0 until croppedSign.cols()) {
            val pixel = croppedSign.get(row, col)
            if (pixel[3] > 0) { // Check alpha channel to ensure it's not transparent
                val originalColor = Color(pixel[0].toInt(), pixel[1].toInt(), pixel[2].toInt())
                val approximatedColor = colorScheme.findBestMatchingColor(originalColor)

                // Replace pixel with approximated color
                val rgb = approximatedColor.toRGB()
                pixel[0] = rgb[0]
                pixel[1] = rgb[1]
                pixel[2] = rgb[2]
                debugImage.put(row, col, *pixel)
            } else {
                // Preserve alpha for transparent pixels
                debugImage.put(row, col, 0.0, 0.0, 0.0, 0.0)
            }
        }
    }

    // Convert to BGR for saving
    Imgproc.cvtColor(debugImage, debugImage, Imgproc.COLOR_RGBA2BGR)

    val classloader = Thread.currentThread().contextClassLoader
    val fileLocationFile = classloader.getResourceAsStream("debug_output_location.txt")
        ?: throw IllegalArgumentException("There is no debug_output_location in the resources folder")
    val debugProcessedFileLocation = fileLocationFile.bufferedReader().use { it.readText() } + System.currentTimeMillis() + "_approximated_colors.jpg"

    // Check if the directory exists
    val directory = File(debugProcessedFileLocation.substringBeforeLast("/"))
    if (!directory.exists()) {
        throw IllegalArgumentException("Invalid directory: $debugProcessedFileLocation")
    }

    Imgcodecs.imwrite(debugProcessedFileLocation, debugImage)
    println("Debug image with approximated colors saved to $debugProcessedFileLocation")
}
