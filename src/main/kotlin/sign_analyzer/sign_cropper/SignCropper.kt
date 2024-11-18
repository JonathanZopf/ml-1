import org.hszg.sign_analyzer.SignAnalysisException
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

/**
 * Crops the sign from the background and makes the background transparent.
 * Furthermore, returns the contour of the sign which is the outermost shape of the sign.
 * Gets an uncropped sign in RGBA format and returns the cropped sign in BGRA format.
 * @param originalSign The original sign image in RGB format.
 * @return The cropped sign with a transparent background.
 */
fun cropSign(originalSign: Mat): Pair<Mat, MatOfPoint> {
    // Convert the image to binary based on the grayscale
    val gray = Mat()
    Imgproc.cvtColor(originalSign, gray, Imgproc.COLOR_RGBA2GRAY)

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

    return Pair(getSignWithOutsideTransparent(originalSign, largestContour), largestContour)
}

/**
 * Makes all pixels outside the convex hull transparent and returns the modified image.
 * @param sign The original sign image in RGB format.
 * @param hull The convex hull that defines the sign.
 * @return The modified image with a transparent background.
 */
private fun getSignWithOutsideTransparent(sign: Mat, hull: MatOfPoint): Mat {
    // Create a mask based on the convex hull
    val mask = Mat.zeros(sign.size(), CvType.CV_8UC1)
    Imgproc.drawContours(mask, listOf(hull), -1, Scalar(255.0), Imgproc.FILLED)

    // Iterate over each pixel and set alpha to 0 where it's outside the convex hull
    for (row in 0 until sign.rows()) {
        for (col in 0 until sign.cols()) {
            val pixelMask = mask.get(row, col)[0]
            if (pixelMask == 0.0) { // Outside the hull
                sign.put(row, col, 0.0, 0.0, 0.0, 0.0) // Make transparent
            }
        }
    }

    return sign
}
