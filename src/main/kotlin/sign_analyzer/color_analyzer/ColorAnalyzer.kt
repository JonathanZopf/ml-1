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
fun analyzeColors(croppedSign: Mat, contour: MatOfPoint) : Pair<List<SignColor>, WhiteCenterAnalyzingResult>{
    if (croppedSign.empty()){
        throw IllegalArgumentException("The input image is empty")
    }

    val colorScheme = findBestColorScheme(croppedSign)
    val colors = getColors(croppedSign, colorScheme)
    val whiteCenter = getWhiteCenter(croppedSign, colorScheme)

    saveDebugCenterSymbol(croppedSign, contour)

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

/**
 * Creates a material where the original colors are replaced by the approximated colors.
 * Also checks for the color scheme that minimizes the distance to the colors of the sign.
 * Transparent pixels are not changed.
 * @param sign The sign to analyze.
 * @return The sign with the approximated colors.
 */
private fun getSignWithApproximatedColors(sign: Mat) : Mat {
    val colorScheme = findBestColorScheme(sign)
    val pixels = getAllPixelColorsOfSignWithRowAndColumn(sign)
    for ((row, col, color) in pixels) {
        val approximatedColor = colorScheme.findBestMatchingColor(color)
        val approximatedColorValues = approximatedColor.getRGBAArray()
        sign.put(row, col, *approximatedColorValues)
    }
    return sign
}
fun findInnermostPoint(contour: MatOfPoint, imageSize: Size): Point {
    // Step 1: Create a binary mask
    val mask = Mat.zeros(imageSize, CvType.CV_8U)
    val contours = listOf(contour)
    Imgproc.drawContours(mask, contours, -1, Scalar(255.0), -1) // Filled contour

    // Step 2: Apply the Distance Transform
    val distTransform = Mat()
    Imgproc.distanceTransform(mask, distTransform, Imgproc.DIST_L2, Imgproc.DIST_MASK_PRECISE)

    // Step 3: Find the maximum distance and its location
    val minMaxLoc = Core.minMaxLoc(distTransform)
    val innermostPoint = minMaxLoc.maxLoc // Point of the maximum distance

    return innermostPoint
}
private fun getCenterSymbolOfSign(sign: Mat, contour: MatOfPoint): Mat {
    // Step 1: Get all pixel colors
    val pixels = getAllPixelColorsOfSignWithRowAndColumn(sign)

    // Step 2: Find the innermost point of the contour
    val innermostPoint = findInnermostPoint(contour, sign.size())
    val startRow = innermostPoint.y.toInt()
    val startCol = innermostPoint.x.toInt()

    // Step 3: Convert sign to RGB because the floodFill function requires a 3-channel image
    val rgbSign = Mat()
    Imgproc.cvtColor(sign, rgbSign, Imgproc.COLOR_RGBA2RGB)

    // Step 4: Create a mask for flood-fill
    val mask = Mat.zeros(Size(sign.cols() + 2.0, sign.rows() + 2.0), CvType.CV_8U) // +2 for border requirements of floodFill

    // Step 5: Create a Mat to store the result
    val result = Mat.zeros(sign.size(), sign.type())

    // Step 6: Perform flood fill to find all connected pixels of the same color
    Imgproc.floodFill(
        rgbSign, // Input image
        mask, // Mask
        Point(startCol.toDouble(), startRow.toDouble()), // Starting point
        Scalar(0.0), // No color change
        Rect(), // Bounding box (not used here)
        Scalar(0.0, 0.0, 0.0, 0.0), // Lower color difference (exact match)
        Scalar(0.0, 0.0, 0.0, 0.0), // Upper color difference (exact match)
        Imgproc.FLOODFILL_MASK_ONLY or Imgproc.FLOODFILL_FIXED_RANGE
    )

    // Step 7: Add the selected pixels to the result Mat
    for ((row, col, color) in pixels) {
        if (mask[row + 1, col + 1][0] > 0) { // Mask offset by 1 due to floodFill requirements
            result.put(row, col, color.red.toDouble(), color.green.toDouble(), color.blue.toDouble(), 255.0)
        }
    }

    // Mark the innermost point
    Imgproc.circle(result, innermostPoint, 10, Scalar(0.0, 0.0, 255.0), -1)

    return result
}



fun saveDebugCenterSymbol( sourceImage: Mat, contour : MatOfPoint) {
    val centerSymbol = getCenterSymbolOfSign(getSignWithApproximatedColors(sourceImage), contour)

    val classloader = Thread.currentThread().contextClassLoader
    val fileLocationFile = classloader.getResourceAsStream("debug_output_location.txt")
        ?: throw IllegalArgumentException("There is no debug_output_location in the resources folder")
    val debugProcessedFileLocation = fileLocationFile.bufferedReader().use { it.readText() } + System.currentTimeMillis() + ".jpg"

    Imgcodecs.imwrite(debugProcessedFileLocation, centerSymbol)
}