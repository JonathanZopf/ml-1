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
   return getAllColorsWithShare(croppedSign)
}

/**
 * Splits the sign in 2 halves and calculates the share of each color on the left and right side of the sign.
 * @param croppedSign The cropped sign to analyze.
 * @param verticalLine The vertical line that splits the sign in 2 halves.
 * @return A pair of SignColor objects representing the colors on the sign. The first element is the left side, the second the right side.
 */
fun analyzeColorsLeftRight(croppedSign: Mat, verticalLine: Pair<Point, Point>) : Pair<List<SignColor>, List<SignColor>> {
    val leftSide = Mat()
    val rightSide = Mat()
    val leftSideRect = Rect(0, 0, verticalLine.first.x.toInt(), croppedSign.rows())
    val rightSideRect = Rect(verticalLine.first.x.toInt(), 0, croppedSign.cols() - verticalLine.first.x.toInt(), croppedSign.rows())
    croppedSign.submat(leftSideRect).copyTo(leftSide)
    croppedSign.submat(rightSideRect).copyTo(rightSide)

    val leftColors = analyzeColors(leftSide)
    val rightColors = analyzeColors(rightSide)
    return Pair(leftColors, rightColors)
}

/**
 * Analyzes the colors of a sign (or a part) and returns a list of approximated colors with their share on the sign.
 * Finds all colors on the sign and approximates them to the closest color from the ApproximatedColor enum.
 * @param partToAnalyze The part of the sign to analyze. Can be the whole sign or a part of it.
 * @return A list of pairs, each containing an approximated color and its share on the sign.
 */
fun getAllColorsWithShare(partToAnalyze: Mat) : List<SignColor> {
    // Count the occurrences of each color and save the count of pixels in a hashmap
    val counted = HashMap<ApproximatedColor, Int>()
    var nonTransparentPixels = 0
    for (row in 0 until partToAnalyze.rows()) {
        for (col in 0 until partToAnalyze.cols()) {
            val pixel = partToAnalyze.get(row, col)
            if (pixel[3] > 0) {
                nonTransparentPixels++
                val color = getApproximatedColor(Color(pixel[2].toInt(), pixel[1].toInt(), pixel[0].toInt()))
                counted[color] = counted.getOrDefault(color, 0) + 1
            }
        }
    }

    // Calculate the share of each color on the sign and return it as a list
    return counted.map { (color, count) ->
        val share = (count.toDouble() / nonTransparentPixels)
        SignColor(color, share)
    }
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
 * Normalizes the brightness of an image.
 * Stolen from https://stackoverflow.com/questions/56905592/automatic-contrast-and-brightness-adjustment-of-a-color-photo-of-a-sheet-of-pape
 * @param originalSign The original sign image.
 * @param clipHistPercent The percentage of the histogram to clip.
 * @return The normalized image.
 */
private fun normalizeBrightness(originalSign: Mat, clipHistPercent: Double = 1.0): Mat {
    val gray = Mat()
    // Convert image to grayscale
    Imgproc.cvtColor(originalSign, gray, Imgproc.COLOR_BGRA2GRAY)

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
    originalSign.convertTo(autoResult, CvType.CV_8UC1, alpha, beta)

    return autoResult
}