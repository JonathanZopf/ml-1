package org.hszg.sign_analyzer

import org.hszg.sign_analyzer.center_symbol_analyzer.CenterSymbolAnalyzerResult
import org.hszg.sign_analyzer.center_symbol_analyzer.findCorrectedInnermostPoint
import org.hszg.sign_analyzer.center_symbol_analyzer.lineBetweenPointsHasNoTransparentPoints
import org.hszg.sign_analyzer.center_symbol_analyzer.projectLineToImageEdges
import org.hszg.sign_analyzer.color_analyzer.ApproximatedColorSign
import org.hszg.sign_properties.ApproximatedColor
import org.hszg.sign_properties.SignColor
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.util.ArrayList
import kotlin.math.sqrt
/**
 * Analyzes the center symbol of a traffic sign based on its approximated color, contour, and other features.
 *
 * @param croppedSignWithApproximatedColor The cropped traffic sign with approximated colors for processing.
 * @param signColors A list of detected sign colors to help identify the center symbol.
 * @param contour The contour of the traffic sign used to find the center symbol.
 * @return A result object containing the center symbol's normalized color and the pixel distribution in its halves.
 */
fun analyzeCenterSymbol(
    croppedSignWithApproximatedColor: ApproximatedColorSign,
    signColors: List<SignColor>,
    contour: MatOfPoint
): CenterSymbolAnalyzerResult {
    // Extract the center symbol from the sign; if not found, return a default result.
    val centerSymbol = getCenterSymbolOfSign(croppedSignWithApproximatedColor, contour, signColors)
        ?: return CenterSymbolAnalyzerResult(0.5, 0.5, 0.5)

    // Calculate the normalized color of the center symbol for further analysis.
    val centerSymbolColor = getCenterSymbolColorForFeatureVector(signColors)

    // Compute the percentage of non-transparent pixels in the left and right halves of the center symbol.
    val (leftHalfPercentage, rightHalfPercentage) = getPercentageOfNonTransparentPixelsForHalvesInCenterSymbol(centerSymbol, signColors)

    return CenterSymbolAnalyzerResult(centerSymbolColor, leftHalfPercentage, rightHalfPercentage)
}

/**
 * Identifies and extracts the center symbol from a traffic sign using flood-fill based on the innermost point.
 *
 * @param approximatedColorSign The traffic sign with approximated colors.
 * @param contour The contour of the traffic sign used for locating the innermost point.
 * @param signColors List of identified sign colors to determine valid center symbol colors.
 * @return A `Mat` containing the isolated center symbol or null if no valid center symbol is found.
 * @throws IllegalArgumentException If the calculated innermost point is outside the bounds of the sign.
 */
private fun getCenterSymbolOfSign(
    approximatedColorSign: ApproximatedColorSign,
    contour: MatOfPoint,
    signColors: List<SignColor>
): Mat? {
    // Convert the sign into an RGBA image for processing.
    val sign = approximatedColorSign.toRGBAMat()

    // Determine the center symbol's approximated color.
    val centerSymbolColor = getApproximatedColorOfCenterSymbolBasedOnSignColors(signColors) ?: return null

    // Locate the innermost point based on the approximated color and contour.
    val point = findCorrectedInnermostPoint(approximatedColorSign, contour, centerSymbolColor)

    // Convert the image to RGB as floodFill requires a 3-channel image.
    val rgbSign = Mat()
    Imgproc.cvtColor(sign, rgbSign, Imgproc.COLOR_RGBA2RGB)

    // Prepare a mask for the flood-fill operation.
    val mask = Mat.zeros(Size(sign.cols() + 2.0, sign.rows() + 2.0), CvType.CV_8U)

    // Create a result matrix to store the isolated center symbol.
    val result = Mat.zeros(sign.size(), sign.type())

    // Validate that the point lies within the image bounds.
    if (point.x >= sign.cols() || point.y >= sign.rows()) {
        throw IllegalArgumentException("The innermost point is not within the sign")
    }

    // Use flood-fill to identify connected pixels of the same color.
    Imgproc.floodFill(
        rgbSign,
        mask,
        point,
        Scalar(0.0), // Keep color unchanged
        Rect(), // Bounding rectangle (not used)
        Scalar(0.0, 0.0, 0.0, 0.0), // Exact match lower bound
        Scalar(0.0, 0.0, 0.0, 0.0), // Exact match upper bound
        Imgproc.FLOODFILL_MASK_ONLY or Imgproc.FLOODFILL_FIXED_RANGE
    )

    // Populate the result matrix with the identified pixels.
    for (row in 0 until sign.rows()) {
        for (col in 0 until sign.cols()) {
            if (mask[row + 1, col + 1][0] > 0) { // Adjust for mask offset.
                result.put(row, col, 255.0, 255.0, 255.0, 255.0)
            }
        }
    }
    return result
}

/**
 * Determines the approximated color of the center symbol based on the detected sign colors.
 *
 * @param signColors The list of detected colors and their respective shares on the traffic sign.
 * @return The approximated color of the center symbol or null if no valid color is found.
 */
private fun getApproximatedColorOfCenterSymbolBasedOnSignColors(signColors: List<SignColor>) : ApproximatedColor? {
    val blackShare = signColors.find { it.getApproximatedColor() == ApproximatedColor.BLACK }!!.getShareOnSign()
    val redShare = signColors.find { it.getApproximatedColor() == ApproximatedColor.RED }!!.getShareOnSign()
    val blueShare = signColors.find { it.getApproximatedColor() == ApproximatedColor.BLUE }!!.getShareOnSign()

    // If the sign has neither much red nor much blue, it is not a sign with a center symbol
    if (redShare < 0.15 && blueShare < 0.2) {
        return null
    }

    if (redShare > blueShare) {
        // Sign is 'Vorfahren gewähren' so the center symbol is black
        return if (blackShare < 0.02) {
            null
        } else {
            // Sign is 'Vorfahrt von rechts' so the center symbol is white
            ApproximatedColor.BLACK
        }
    }
    // Sign is either 'Fahrtrichtung rechts' or 'Fahrtrichtung links', so the center symbol is white
    return ApproximatedColor.WHITE
}
/**
 * Calculates the normalized intensity of the center symbol's color based on its RGB values.
 * The intensity is scaled between 0 and 1, representing the relative brightness.
 * If no color is found, a default value of 0.5 is returned.
 *
 * @param signColors List of `SignColor` objects representing possible colors in the traffic sign.
 * @return A `Double` representing the normalized brightness of the center symbol's color.
 */
private fun getCenterSymbolColorForFeatureVector(signColors: List<SignColor>): Double {
    // Approximate the dominant color of the center symbol based on pre-defined sign colors
    val centerSymbolColor = getApproximatedColorOfCenterSymbolBasedOnSignColors(signColors)

    // If no valid color is found, return a neutral value
    if (centerSymbolColor == null) {
        return 0.5
    } else {
        // Extract RGBA components of the approximated color
        val colorArray = centerSymbolColor.getRGBAArray()

        // Compute the brightness as the sum of RGB components, normalized by the maximum value
        val max = 3 * 255
        val value = colorArray[0] + colorArray[1] + colorArray[2]
        return value.toDouble() / max
    }
}

/**
 * Extracts the largest contour of the center symbol in the given image.
 * The method converts the image to grayscale, thresholds it, and detects the contour with the largest area.
 *
 * @param centerSymbol A `Mat` object representing the center symbol image in RGBA format.
 * @return A `MatOfPoint` object representing the largest contour of the center symbol.
 * @throws IllegalStateException if no contours are found in the image.
 */
private fun getContourOfCenterSymbol(centerSymbol: Mat): MatOfPoint {
    // Convert the RGBA image to grayscale for binary thresholding
    val grayCenterSymbol = Mat()
    Imgproc.cvtColor(centerSymbol, grayCenterSymbol, Imgproc.COLOR_RGBA2GRAY)

    // Apply a binary threshold to isolate the symbol
    val binaryCenterSymbol = Mat()
    Imgproc.threshold(grayCenterSymbol, binaryCenterSymbol, 0.0, 255.0, Imgproc.THRESH_BINARY)

    // Find contours in the binary image
    val contours = ArrayList<MatOfPoint>()
    val hierarchy = Mat()
    Imgproc.findContours(binaryCenterSymbol, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

    // Return the contour with the largest area, ensuring it's not null
    return contours.maxByOrNull { Imgproc.contourArea(it) }
        ?: throw IllegalStateException("No contours found in the center symbol.")
}

/**
 * Determines a division line for the center symbol based on its contour.
 * The division line connects the lowest point in the contour to the point
 * furthest away that lies entirely within the symbol.
 *
 * @param centerSymbol A `Mat` object representing the center symbol.
 * @return A `Pair<Point, Point>` representing the two endpoints of the division line.
 * @throws IllegalArgumentException if the center symbol contour is empty.
 */
private fun getCenterSymbolDivisionLine(centerSymbol: Mat): Pair<Point, Point> {
    // Obtain the largest contour of the center symbol
    val contour = getContourOfCenterSymbol(centerSymbol)
    if (contour.empty()) throw IllegalArgumentException("Contour is empty!")

    // Convert the contour into a list of points
    val points = contour.toList()

    // Find the lowest point in the contour (maximum y-coordinate)
    val lowestPoint = points.maxByOrNull { it.y }!!

    // Find the point that maximizes the distance from the lowest point while being valid
    val highestPoint = points
        .filter {
            // Ensure the line connecting the two points contains no transparent pixels
            lineBetweenPointsHasNoTransparentPoints(centerSymbol, lowestPoint, it)
        }
        .maxByOrNull { point ->
            // Calculate Euclidean distance from the lowest point
            val dx = point.x - lowestPoint.x
            val dy = point.y - lowestPoint.y
            sqrt(dx * dx + dy * dy)
        }!!

    // Extend the division line to the edges of the image
    return projectLineToImageEdges(lowestPoint, highestPoint, centerSymbol.width(), centerSymbol.height())
}
/**
 * Splits the center symbol into left and right halves based on its division line.
 * The division line is calculated using the largest contour of the symbol.
 *
 * @param centerSymbol A `Mat` object representing the center symbol.
 * @return A `Pair<Mat, Mat>` containing the left and right halves of the center symbol.
 */
private fun splitCenterSymbol(centerSymbol: Mat): Pair<Mat, Mat> {
    // Obtain the division line for the center symbol
    val divisionLine = getCenterSymbolDivisionLine(centerSymbol)

    val width = centerSymbol.cols()
    val height = centerSymbol.rows()

    // Create binary masks for the left and right halves
    val leftMask = Mat.zeros(height, width, CvType.CV_8UC1)
    val rightMask = Mat.zeros(height, width, CvType.CV_8UC1)

    // Define the polygons for left and right masks
    val point1 = divisionLine.first
    val point2 = divisionLine.second
    val polygonLeft = listOf(
        Point(0.0, 0.0), Point(point1.x, point1.y), Point(point2.x, point2.y), Point(0.0, height.toDouble())
    )
    val polygonRight = listOf(
        Point(width.toDouble(), 0.0), Point(point1.x, point1.y), Point(point2.x, point2.y), Point(width.toDouble(), height.toDouble())
    )

    // Fill the polygons into the respective masks
    Imgproc.fillPoly(leftMask, listOf(MatOfPoint(*polygonLeft.toTypedArray())), Scalar(255.0))
    Imgproc.fillPoly(rightMask, listOf(MatOfPoint(*polygonRight.toTypedArray())), Scalar(255.0))

    // Apply the masks to split the original image
    val leftHalf = Mat.zeros(height, width, CvType.CV_8UC4)
    val rightHalf = Mat.zeros(height, width, CvType.CV_8UC4)
    Core.bitwise_and(centerSymbol, centerSymbol, leftHalf, leftMask)
    Core.bitwise_and(centerSymbol, centerSymbol, rightHalf, rightMask)

    return Pair(leftHalf, rightHalf)
}


/**
 * Counts the number of non-transparent pixels in a given half of the center symbol.
 *
 * @param centerSymbolHalf A `Mat` object representing one half of the center symbol.
 * @return An `Int` representing the count of non-transparent pixels.
 */
private fun getNonTransparentPixelsInCenterSymbolHalf(centerSymbolHalf: Mat): Int {
    var pixels = 0
    for (row in 0 until centerSymbolHalf.rows()) {
        for (col in 0 until centerSymbolHalf.cols()) {
            // Check if the alpha channel value indicates transparency
            val pixel = centerSymbolHalf.get(row, col)
            if (pixel[3] > 0.0) { // Alpha channel > 0 indicates a non-transparent pixel
                pixels++
            }
        }
    }
    return pixels
}

/**
 * Calculates the percentage of non-transparent pixels in each half of the center symbol.
 *
 * @param centerSymbol A `Mat` object representing the center symbol.
 * @param signColors A list of detected sign colors to help identify the center symbol. If the center symbol is not white, the percentages default to 0.5.
 * @return A `Pair<Double, Double>` where the first value is the percentage for the left half
 *         and the second value is the percentage for the right half.
 *         If no pixels are found, both percentages default to 0.5.
 */
private fun getPercentageOfNonTransparentPixelsForHalvesInCenterSymbol(centerSymbol: Mat, signColors: List<SignColor>): Pair<Double, Double> {
    // If the center symbol is not white, return default values
    if (getApproximatedColorOfCenterSymbolBasedOnSignColors(signColors) != ApproximatedColor.WHITE) {
        return Pair(0.5, 0.5)
    }

    // Split the center symbol into left and right halves
    val (leftHalf, rightHalf) = splitCenterSymbol(centerSymbol)

    // Count non-transparent pixels in each half
    val leftHalfPixels = getNonTransparentPixelsInCenterSymbolHalf(leftHalf)
    val rightHalfPixels = getNonTransparentPixelsInCenterSymbolHalf(rightHalf)
    val totalPixels = leftHalfPixels + rightHalfPixels

    // Calculate percentages, defaulting to 0.5 if no pixels exist
    val res = Pair(leftHalfPixels.toDouble() / totalPixels, rightHalfPixels.toDouble() / totalPixels)
    if (res.first.isNaN() || res.second.isNaN()) {
        return Pair(0.5, 0.5)
    }
    return res
}