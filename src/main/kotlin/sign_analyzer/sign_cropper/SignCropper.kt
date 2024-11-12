import org.hszg.sign_analyzer.SignAnalysisException
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

/**
 * Crops the sign from the background and makes the background transparent.
 * Gets an uncropped sign in RGB format and returns the cropped sign in BGRA format.
 * @param originalSign The original sign image in RGB format.
 * @return The cropped sign with a transparent background.
 */
fun cropSign(originalSign: Mat): Mat {
    // Convert the image to binary based on the grayscale
    val gray = Mat()
    Imgproc.cvtColor(originalSign, gray, Imgproc.COLOR_BGR2GRAY)

    // Apply Canny edge detection
    val edges = Mat()
    Imgproc.Canny(gray, edges, 100.0, 200.0)

    // Find contours
    val contours = ArrayList<MatOfPoint>()
    val hierarchy = Mat()
    Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

    // Find the largest contour
    var largestContour: MatOfPoint? = null
    var largestArea = Double.MIN_VALUE
    for (contour in contours) {
        val area = Imgproc.contourArea(contour)
        if (area > largestArea) {
            largestArea = area
            largestContour = contour
        }
    }

    // If no contour was found, throw an exception
    if (largestContour == null) {
        throw SignAnalysisException("No contour found")
    }

    // Get the convex hull of the largest contour. This is used to "complete" the contour, if there is a gap which is likely in low-resolution images.
    val convexHull = MatOfPoint()
    val hullIndices = MatOfInt()
    Imgproc.convexHull(largestContour, hullIndices)
    val hullPoints = hullIndices.toArray().map { largestContour.toArray()[it] }
    convexHull.fromList(hullPoints)

    return getSignWithOutsideTransparent(originalSign, convexHull)
}

/**
 * Makes all pixels outside the convex hull transparent and returns the modified image.
 * Converts the image to BGRA format.
 * @param rgbSign The original sign image in RGB format.
 * @param hull The convex hull that defines the sign.
 * @return The modified image with a transparent background.
 */
private fun getSignWithOutsideTransparent(rgbSign: Mat, hull: MatOfPoint): Mat {
    val rgbaSign = Mat()
    Imgproc.cvtColor(rgbSign, rgbaSign, Imgproc.COLOR_BGR2BGRA)

    // Create a mask based on the convex hull
    val mask = Mat.zeros(rgbaSign.size(), CvType.CV_8UC1)
    Imgproc.drawContours(mask, listOf(hull), -1, Scalar(255.0), Imgproc.FILLED)

    // Iterate over each pixel and set alpha to 0 where it's outside the convex hull
    for (row in 0 until rgbaSign.rows()) {
        for (col in 0 until rgbaSign.cols()) {
            val pixelMask = mask.get(row, col)[0]
            if (pixelMask == 0.0) { // Outside the hull
                rgbaSign.put(row, col, 0.0, 0.0, 0.0, 0.0) // Make transparent
            }
        }
    }

    return rgbaSign
}
