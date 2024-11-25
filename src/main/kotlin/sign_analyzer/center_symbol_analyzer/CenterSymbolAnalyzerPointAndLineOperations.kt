package org.hszg.sign_analyzer.center_symbol_analyzer

import org.hszg.sign_analyzer.color_analyzer.ApproximatedColorSign
import org.hszg.sign_properties.ApproximatedColor
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
/**
 * Finds the initial innermost point within a given contour by using a distance transform.
 *
 * @param contour The contour represented as a MatOfPoint.
 * @param imageSize The size of the image containing the contour.
 * @return A Point representing the innermost location within the contour,
 *         where the distance to the edge of the contour is maximized.
 * @throws IllegalArgumentException if the contour or imageSize is invalid.
 */
fun findInitialInnermostPointFromContour(contour: MatOfPoint, imageSize: Size): Point {
    // Step 1: Create a binary mask of the same size as the image.
    val mask = Mat.zeros(imageSize, CvType.CV_8U)
    val contours = listOf(contour)
    Imgproc.drawContours(mask, contours, -1, Scalar(255.0), -1) // Fill the contour with white color.

    // Step 2: Compute the distance transform of the binary mask.
    val distTransform = Mat()
    Imgproc.distanceTransform(mask, distTransform, Imgproc.DIST_L2, Imgproc.DIST_MASK_PRECISE)

    // Step 3: Find the point with the maximum distance to the contour edge.
    val minMaxLoc = Core.minMaxLoc(distTransform)
    val innermostPoint = minMaxLoc.maxLoc // Point with the maximum distance.

    return innermostPoint
}

/**
 * Refines the innermost point by finding the closest pixel matching a specific color.
 *
 * @param croppedSignWithApproximatedColor The image containing the contour, with approximated color details.
 * @param contour The contour within the image.
 * @param approximatedColorOfCenterSymbol The color to which the innermost point is refined.
 * @return A Point representing the nearest pixel of the specified color to the initial innermost point.
 * @throws IllegalStateException if no matching pixel is found.
 */
fun findCorrectedInnermostPoint(
    croppedSignWithApproximatedColor: ApproximatedColorSign,
    contour: MatOfPoint,
    approximatedColorOfCenterSymbol: ApproximatedColor
): Point {
    // Get the initial innermost point from the contour.
    val initialInnermostPoint = findInitialInnermostPointFromContour(contour, croppedSignWithApproximatedColor.getSize())

    // Search for the nearest pixel of the approximated color to the innermost point.
    var nearestWhiteOrBlackPixel: Pair<Point?, Double> = Pair(null, Double.MAX_VALUE)
    val height = croppedSignWithApproximatedColor.getSize().height
    val width = croppedSignWithApproximatedColor.getSize().width

    for (item in croppedSignWithApproximatedColor.getPixels()) {
        if (item.getColor() == approximatedColorOfCenterSymbol) {
            val row = item.getRow()
            val col = item.getCol()
            val distance = sqrt((row - initialInnermostPoint.y).pow(2) + (col - initialInnermostPoint.x).pow(2))

            // Ensure the pixel is within valid bounds to avoid an out-of-bounds error.
            val isWithinSize = row >= 0 && row < height && col >= 0 && col < width
            if (distance < nearestWhiteOrBlackPixel.second && isWithinSize) {
                nearestWhiteOrBlackPixel = Pair(Point(col.toDouble(), row.toDouble()), distance)
            }
        }
    }

    // Ensure a valid pixel is found; otherwise, throw an exception.
    return nearestWhiteOrBlackPixel.first
        ?: throw IllegalStateException("No matching pixel found within the specified color range.")
}

/**
 * Checks if a line between two points has no transparent pixels in an image.
 *
 * @param centerSymbol The image represented as a Mat.
 * @param point1 The starting point of the line.
 * @param point2 The ending point of the line.
 * @return True if no transparent pixels are encountered along the line; false otherwise.
 */
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

    // Iterate through the line using Bresenham's algorithm.
    while (true) {
        // Check if the pixel at the current location is transparent.
        val colorOfPoint = centerSymbol.get(y, x)
        if (!(colorOfPoint[3] > 0.0)) { // Check the alpha channel.
            return false
        }

        // Stop when the line reaches the endpoint.
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

/**
 * Projects a line segment defined by two points onto the edges of an image.
 *
 * @param p1 The first point of the line.
 * @param p2 The second point of the line.
 * @param imageWidth The width of the image.
 * @param imageHeight The height of the image.
 * @return A Pair of Points representing the intersections of the line with the image edges.
 */
fun projectLineToImageEdges(p1: Point, p2: Point, imageWidth: Int, imageHeight: Int): Pair<Point, Point> {
    val top = 0.0
    val bottom = imageHeight.toDouble()
    val left = 0.0
    val right = imageWidth.toDouble()

    val dx = p2.x - p1.x
    val dy = p2.y - p1.y

    // Handle vertical lines separately to avoid division by zero.
    if (dx == 0.0) {
        return Pair(
            Point(p1.x, top),
            Point(p1.x, bottom)
        )
    }

    val slope = dy / dx
    val intercept = p1.y - slope * p1.x

    val intersections = mutableListOf<Point>()

    // Intersection with top edge (y = 0).
    val xAtTop = -intercept / slope
    if (xAtTop in left..right) intersections.add(Point(xAtTop, top))

    // Intersection with bottom edge (y = imageHeight).
    val xAtBottom = (bottom - intercept) / slope
    if (xAtBottom in left..right) intersections.add(Point(xAtBottom, bottom))

    // Intersection with left edge (x = 0).
    val yAtLeft = intercept
    if (yAtLeft in top..bottom) intersections.add(Point(left, yAtLeft))

    // Intersection with right edge (x = imageWidth).
    val yAtRight = slope * right + intercept
    if (yAtRight in top..bottom) intersections.add(Point(right, yAtRight))

    return Pair(intersections[0], intersections[1])
}
