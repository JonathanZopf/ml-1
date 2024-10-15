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
fun analyzeColors(croppedSign: Mat) : List<SignColor>{
    normalizeBrightness(croppedSign)
    // Get all colors (java color) on the sign and their share
    val colorShareUnapproximated = getAllColorsWithShareOfSign(croppedSign)

    // Get the approximated colors and initialize the map with 0.0 share
    val approximatedColors = mutableMapOf<ApproximatedColor, Double>().apply {
        ApproximatedColor.entries.forEach { put(it, .0) }  // Initialize with 0.0
    }

    // For each color on the sign, approximate it and add the share to the corresponding approximated color
    for (currentColor in colorShareUnapproximated) {
        val approximatedColor = approximateColor(currentColor.first)
        // Update the map by adding the share to the corresponding approximated color
        approximatedColors[approximatedColor] = approximatedColors[approximatedColor]!! + currentColor.second
    }

    // Return the result as a list of SignColor objects
    // TODO Calculate the share of the colors on the left and right side of the sign
    return approximatedColors.toList().map { (approximatedColor, share) ->
        SignColor(approximatedColor, share, 1.0, 0.0)
    }
}

/**
 * Finds all colors on a sign and calculates their share on the sign.
 * @param croppedSign The cropped sign to analyze.
 * @return A list of pairs containing the color (Java color) and its share on the sign.
 */
fun getAllColorsWithShareOfSign(croppedSign: Mat) : List<Pair<Color, Double>> {
    // Count the occurrences of each color and save the count of pixels in a hashmap
    val counted = HashMap<Color, Int>()
    var nonTransparentPixels = 0
    for (row in 0 until croppedSign.rows()) {
        for (col in 0 until croppedSign.cols()) {
            val pixel = croppedSign.get(row, col)
            if (pixel[3] > 0) {
                nonTransparentPixels++
                val color = Color(pixel[2].toInt(), pixel[1].toInt(), pixel[0].toInt())
                counted[color] = counted.getOrDefault(color, 0) + 1
            }
        }
    }

    // Calculate the share of each color on the sign and return it as a list
    return counted.map { (color, count) ->
        val share = (count.toDouble() / nonTransparentPixels)
        Pair(color, share)
    }
}


/**
 * Approximates a given color to the closest color from the [ApproximatedColor] enum.
 * @param originalColor The color to approximate.
 * @return The approximated color from the [ApproximatedColor] enum.
 */
fun approximateColor(color: Color) : ApproximatedColor {
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

fun automaticBrightnessAndContrast(image: Mat, clipHistPercent: Double = 1.0): Mat {
    val gray = Mat()
    // Convert image to grayscale
    Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY)

    // Calculate grayscale histogram
    val histSize = MatOfInt(256)
    val histRange = MatOfFloat(0f, 256f)
    val hist = Mat()
    Imgproc.calcHist(listOf(gray), MatOfInt(0), Mat(), hist, histSize, histRange)

    val accumulator = mutableListOf<Double>()
    accumulator.add(hist.get(0, 0)[0])

    // Calculate cumulative distribution from the histogram
    for (i in 1 until hist.rows()) {
        accumulator.add(accumulator[i - 1] + hist.get(i, 0)[0])
    }

    // Locate points to clip
    val maximum = accumulator.last()
    var clipHistPercent = clipHistPercent * (maximum / 100.0) / 2.0

    // Locate left cut
    var minimumGray = 0
    while (accumulator[minimumGray] < clipHistPercent) {
        minimumGray++
    }

    // Locate right cut
    var maximumGray = accumulator.size - 1
    while (accumulator[maximumGray] >= maximum - clipHistPercent) {
        maximumGray--
    }

    // Calculate alpha and beta
    val alpha = 255.0 / (maximumGray - minimumGray)
    val beta = -minimumGray * alpha

    // Adjust image brightness and contrast
    val autoResult = Mat()
    image.convertTo(autoResult, CvType.CV_8UC3, alpha, beta)

    return autoResult
}
fun normalizeBrightness(originalImage: Mat): Mat {
    val adjustedImage = automaticBrightnessAndContrast(originalImage)
    Imgcodecs.imwrite("/Users/jonathan/Downloads/Verkehrszeichen/debug/adj_"+System.currentTimeMillis()+".jpg", adjustedImage)
    return adjustedImage
}