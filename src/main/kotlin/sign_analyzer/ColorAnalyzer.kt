package org.hszg.sign_analyzer

import org.hszg.sign_properties.ApproximatedColor
import org.hszg.sign_properties.SignColor
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.awt.Color

/**
 * Analyzes the colors of a cropped sign and returns a list of SignColor objects.
 * Finds all colors on the sign and approximates them to the closest color from the ApproximatedColor enum.
 * Calculates the share of each approximated color on the sign as well as their share on the left and right side of the sign.
 * @param croppedSign The cropped sign to analyze.
 * @return A list of SignColor objects representing the colors on the sign.
 */
fun analyzeColors(croppedSign: Mat) : List<SignColor>{
    // Get all colors (java color) on the sign and their share
    val colorShareUnapproximated = getAllColorsWithShareOfSign(croppedSign)

    // Get the approximated colors and initialize the map with 0.0 share
    val approximatedColors = mutableMapOf<ApproximatedColor, Double>().apply {
        ApproximatedColor.entries.forEach { put(it, 0.0) }  // Initialize with 0.0
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
    for (row in 0 until croppedSign.rows()) {
        for (col in 0 until croppedSign.cols()) {
            val pixel = croppedSign.get(row, col)
            if (pixel[3] > 0) {
                val color = Color(pixel[2].toInt(), pixel[1].toInt(), pixel[0].toInt())
                counted[color] = counted.getOrDefault(color, 0) + 1
            }
        }
    }

    // Calculate the share of each color on the sign and return it as a list
    val totalPixels = croppedSign.rows() * croppedSign.cols()
    return counted.map { (color, count) ->
        val share = (count.toDouble() / totalPixels) * 100.0
        Pair(color, share)
    }
}

/**
 * Approximates a given color to the closest color from the [ApproximatedColor] enum.
 * @param originalColor The color to approximate.
 * @return The approximated color from the [ApproximatedColor] enum.
 */
fun approximateColor(originalColor: Color) : ApproximatedColor {
    var bestMatch = Pair(ApproximatedColor.RED, Double.MAX_VALUE)
    for (approximatedColor in ApproximatedColor.entries) {
        val distance = approximatedColor.calculateDistanceFromColor(originalColor)
        if (distance < bestMatch.second) {
            bestMatch = Pair(approximatedColor, distance)
        }
    }
    return bestMatch.first
}
