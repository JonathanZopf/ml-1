import org.hszg.sign_analyzer.color_analyzer.ColorScheme
import org.hszg.sign_analyzer.color_analyzer.WhiteCenterAnalyzingResult
import org.hszg.sign_properties.ApproximatedColor
import org.hszg.sign_properties.SignColor
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.awt.Color
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.*

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

    val centerSymbol = getCenterSymbolOfSign(getSignWithApproximatedColors(croppedSign), contour, colors)
    if (centerSymbol != null) {
        val percentage = getPercentageOfNonTransparentPixelsForHalvesInCenterSymbol(centerSymbol)
        println("Percentage of non-transparent pixels in left half: ${percentage.first}")
        println("Percentage of non-transparent pixels in right half: ${percentage.second}")
        saveDebugCenterSymbol(centerSymbol)
    }

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

private fun findInitialInnermostPointFromContour(contour: MatOfPoint, imageSize: Size): Point {
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
fun findCorrectedInnermostPoint(contour: MatOfPoint, imageSize: Size, sign: Mat, approximatedColorOfCenterSymbol: ApproximatedColor): Point {
    val initialInnermostPoint = findInitialInnermostPointFromContour(contour, imageSize)
    // Find the closest white or black pixel to the innermost point
    val colorScheme = findBestColorScheme(sign)
    val pixels = getAllPixelColorsOfSignWithRowAndColumn(sign)
    var nearestWhiteOrBlackPixel: Pair<Point?, Double> = Pair(null, Double.MAX_VALUE)
    for ((row, col, color) in pixels) {
        val approximatedColor = colorScheme.findBestMatchingColor(color)
        if (approximatedColor == approximatedColorOfCenterSymbol) {
            val distance = sqrt((row - initialInnermostPoint.y).pow(2) + (col - initialInnermostPoint.x).pow(2))
            if (distance < nearestWhiteOrBlackPixel.second) {
                nearestWhiteOrBlackPixel = Pair(Point(col.toDouble(), row.toDouble()), distance)
            }
        }
    }
    return nearestWhiteOrBlackPixel.first!!
}
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
private fun getCenterSymbolColorForFeatureVector(signColors: List<SignColor>) : Double{
    val centerSymbolColor = getApproximatedColorOfCenterSymbolBasedOnSignColors(signColors)
    if (centerSymbolColor == null) {
        return 0.5
    } else {
        val max = centerSymbolColor.getRGBAArray().size * 255.0
        val average = centerSymbolColor.getRGBAArray().average()
        return average / max
    }
}
private fun getCenterSymbolOfSign(sign: Mat, contour: MatOfPoint, signColors: List<SignColor>): Mat? {
    // Step 1: Get all pixel colors
    val pixels = getAllPixelColorsOfSignWithRowAndColumn(sign)

    // Step 2: Find the innermost point of the contour
    val centerSymbolColor = getApproximatedColorOfCenterSymbolBasedOnSignColors(signColors) ?: return null
    val innermostPoint = findCorrectedInnermostPoint(contour, sign.size(), sign, centerSymbolColor)
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
            result.put(row, col, 255.0, 255.0, 255.0, 255.0)
        }
    }

    return result
}

private fun getContourOfCenterSymbol(centerSymbol: Mat): MatOfPoint {
    val grayCenterSymbol = Mat()
    Imgproc.cvtColor(centerSymbol, grayCenterSymbol, Imgproc.COLOR_RGBA2GRAY)

    val binaryCenterSymbol = Mat()
    Imgproc.threshold(grayCenterSymbol, binaryCenterSymbol, 0.0, 255.0, Imgproc.THRESH_BINARY)

    val contours = ArrayList<MatOfPoint>()
    val hierarchy = Mat()
    Imgproc.findContours(binaryCenterSymbol, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

    return contours.maxByOrNull { Imgproc.contourArea(it) }!!

}

private fun getCenterSymbolDivisionLine(centerSymbol: Mat): Pair<Point, Point> {
    val contour = getContourOfCenterSymbol(centerSymbol)
    if (contour.empty()) throw IllegalArgumentException("Contour is empty!")

    // Convert contour to a list of points
    val points = contour.toList()

    // Find the lowest point (max y-coordinate)
    val lowestPoint = points.maxByOrNull { it.y }!!

    // Find the point that maximizes distance while staying within the contour
    val highestPoint = points
        .filter {
            lineBetweenPointsHasNoTransparentPoints(centerSymbol, lowestPoint, it)
        }
        .maxByOrNull { point ->
            val dx = point.x - lowestPoint.x
            val dy = point.y - lowestPoint.y
            sqrt(dx * dx + dy * dy)
        }!!

    return projectLineToImageEdges(lowestPoint, highestPoint, centerSymbol.width(), centerSymbol.height())
}

private fun splitCenterSymbol(centerSymbol: Mat): Pair<Mat, Mat> {
    val divisionLine = getCenterSymbolDivisionLine(centerSymbol) // Assume this returns Pair<Point, Point>

    val width = centerSymbol.cols()
    val height = centerSymbol.rows()

    // Create masks for left and right halves
    val leftMask = Mat.zeros(height, width, CvType.CV_8UC1)
    val rightMask = Mat.zeros(height, width, CvType.CV_8UC1)

    // Points of the line
    val point1 = divisionLine.first
    val point2 = divisionLine.second

    // Fill masks with polygons
    val polygonLeft = listOf(
        Point(0.0, 0.0),
        Point(point1.x, point1.y),
        Point(point2.x, point2.y),
        Point(0.0, height.toDouble())
    )
    val polygonRight = listOf(
        Point(width.toDouble(), 0.0),
        Point(point1.x, point1.y),
        Point(point2.x, point2.y),
        Point(width.toDouble(), height.toDouble())
    )

    Imgproc.fillPoly(leftMask, listOf(MatOfPoint(*polygonLeft.toTypedArray())), Scalar(255.0))
    Imgproc.fillPoly(rightMask, listOf(MatOfPoint(*polygonRight.toTypedArray())), Scalar(255.0))

    // Prepare output images
    val leftHalf = Mat.zeros(height, width, CvType.CV_8UC4)
    val rightHalf = Mat.zeros(height, width, CvType.CV_8UC4)

    // Apply masks to the original image
    Core.bitwise_and(centerSymbol, centerSymbol, leftHalf, leftMask) // Ensure RGBA is preserved
    Core.bitwise_and(centerSymbol, centerSymbol, rightHalf, rightMask)

    return Pair(leftHalf, rightHalf)
}


private fun getNonTransparentPixelsInCenterSymbolHalf(centerSymbolHalf: Mat) : Int {
    var pixels = 0;
    for (row in 0 until centerSymbolHalf.rows()) {
        for (col in 0 until centerSymbolHalf.cols()) {
            val pixel = centerSymbolHalf.get(row, col)
            if (pixel[3] > 0.0) {
                pixels++
            }
        }
    }
    return pixels
}
private fun getPercentageOfNonTransparentPixelsForHalvesInCenterSymbol(centerSymbol: Mat): Pair<Double, Double> {
    val (leftHalf, rightHalf) = splitCenterSymbol(centerSymbol)
    val leftHalfPixels = getNonTransparentPixelsInCenterSymbolHalf(leftHalf)
    val rightHalfPixels = getNonTransparentPixelsInCenterSymbolHalf(rightHalf)
    val totalPixels = leftHalfPixels + rightHalfPixels
    return Pair(leftHalfPixels.toDouble() / totalPixels, rightHalfPixels.toDouble() / totalPixels)
}
fun lineBetweenPointsHasNoTransparentPoints(centerSymbol: Mat, point1: Point, point2: Point): Boolean {
    val x1 = point1.x.toInt()
    val y1 = point1.y.toInt()
    val x2 = point2.x.toInt()
    val y2 = point2.y.toInt()

    var dx = abs(x2 - x1)
    var dy = -abs(y2 - y1)
    val sx = if (x1 < x2) 1 else -1
    val sy = if (y1 < y2) 1 else -1
    var err = dx + dy

    var x = x1
    var y = y1

    while (true) {
        // Check transparency of the current pixel
        if (!pointIsNotTransparent(centerSymbol, Point(x.toDouble(), y.toDouble()))) {
            return false
        }

        // Break when the line reaches the endpoint
        if (x == x2 && y == y2) break

        val e2 = 2 * err
        if (e2 >= dy) {
            err += dy
            x += sx
        }
        if (e2 <= dx) {
            err += dx
            y += sy
        }
    }

    return true
}
fun projectLineToImageEdges(p1: Point, p2: Point, imageWidth: Int, imageHeight: Int): Pair<Point, Point> {
    // Image boundaries
    val top = 0.0
    val bottom = imageHeight.toDouble()
    val left = 0.0
    val right = imageWidth.toDouble()

    // Line slope (dy/dx) and y-intercept
    val dx = p2.x - p1.x
    val dy = p2.y - p1.y

    // Handle vertical line separately to avoid division by zero
    if (dx == 0.0) {
        // Line is vertical, intersects top and bottom
        return Pair(
            Point(p1.x, top), // Top edge
            Point(p1.x, bottom) // Bottom edge
        )
    }

    val slope = dy / dx
    val intercept = p1.y - slope * p1.x

    // Calculate intersections with the edges of the image
    val intersections = mutableListOf<Point>()

    // Intersection with top edge (y = 0)
    val xAtTop = -intercept / slope
    if (xAtTop in left..right) intersections.add(Point(xAtTop, top))

    // Intersection with bottom edge (y = height)
    val xAtBottom = (bottom - intercept) / slope
    if (xAtBottom in left..right) intersections.add(Point(xAtBottom, bottom))

    // Intersection with left edge (x = 0)
    val yAtLeft = intercept
    if (yAtLeft in top..bottom) intersections.add(Point(left, yAtLeft))

    // Intersection with right edge (x = width)
    val yAtRight = slope * right + intercept
    if (yAtRight in top..bottom) intersections.add(Point(right, yAtRight))

    // Ensure exactly two points are returned
    require(intersections.size == 2) { "Line does not intersect the image edges correctly." }

    return Pair(intersections[0], intersections[1])
}



private fun pointIsNotTransparent(centerSymbol: Mat, point: Point): Boolean {
    val pixel = centerSymbol.get(point.y.toInt(), point.x.toInt())
    return pixel[3] > 0.0
}

fun saveDebugCenterSymbol(centerSymbol: Mat) {
    val classloader = Thread.currentThread().contextClassLoader
    val fileLocationFile = classloader.getResourceAsStream("debug_output_location.txt")
        ?: throw IllegalArgumentException("There is no debug_output_location in the resources folder")
    val debugProcessedFileLocation = fileLocationFile.bufferedReader().use { it.readText() } + System.currentTimeMillis() + ".jpg"

    //draw division line
    val divisionLine = getCenterSymbolDivisionLine(centerSymbol)
    Imgproc.line(centerSymbol, divisionLine.first, divisionLine.second, Scalar(0.0, 0.0, 255.0), 1)

    Imgproc.cvtColor(centerSymbol, centerSymbol, Imgproc.COLOR_RGBA2BGR)
    Imgcodecs.imwrite(debugProcessedFileLocation, centerSymbol)
}