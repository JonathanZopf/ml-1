package org.hszg.sign_analyzer

import analyzeColors
import cropSign
import org.hszg.SignLoading.LoadableSign
import org.hszg.sign_analyzer.color_analyzer.WhiteCenterAnalyzingResult
import org.hszg.sign_analyzer.extremities_finder.findCorners
import org.hszg.sign_analyzer.extremities_finder.findOutermostCorners
import org.hszg.sign_analyzer.shape_recognizer.recognizeShape
import org.hszg.sign_properties.SignColor
import org.hszg.sign_properties.SignProperties
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sqrt


@Throws(SignAnalysisException::class)
fun analyzeSign(loadableSign: LoadableSign, writeDebugImage : Boolean = false) : SignProperties {
    try {
        val sign = loadableSign.loadImage()
        val croppedSignWithContour = cropSign(sign)
        val croppedSign = croppedSignWithContour.first
        val contour = croppedSignWithContour.second

        if (writeDebugImage) {
            writeCroppedSign(croppedSign)
        }

        // Corners are the extreme points of the sign. They give a rough idea of the shape of the sign. There are arbitrary in number.
        val corners = findCorners(sign)

        // Outermost corners are the corners of the sign that are the farthest away from each other. They are used to calculate the vertical and horizontal line of the sign. Are always 4 corners.
        val outermostCorners = findOutermostCorners(contour)
        val verticalLine = outermostCorners.getVerticalLine()
        val horizontalLine = outermostCorners.getHorizontalLine()

        val colors = analyzeColors(croppedSign)
        val colorsTotalSign = colors.first
        val whiteCenter = colors.second

        if (writeDebugImage) {
            writeDebugResultImage(
                croppedSign,
                corners,
                colorsTotalSign,
                whiteCenter,
                verticalLine,
                horizontalLine
            )
        }
        return SignProperties(
            colors = colorsTotalSign,
            shape = recognizeShape(corners),
            whiteCenter = whiteCenter,
        )
    } catch (e: Exception) {
        throw SignAnalysisException("Error during sign analysis", e)
    }
}

/**
 * Write the debug image with the result of the sign analysis.
 * The image will be written to the directory specified in the resources folder.
 * @param sign The sign image. Will be the base for the debug image.
 * @param extremities The extreme points of the sign. Will be marked with a red circle.
 * @param colorsTotalSign The colors of the sign. Will be written to the image as text.
 * @param whiteCenter The result of the white center analyzing. Will be written to the image as text.
 * @param verticalLine The vertical line of the sign. Will be drawn to the image as a green line.
 */
private fun writeDebugResultImage(sign: Mat, extremities: MatOfPoint, colorsTotalSign: List<SignColor>, whiteCenter: WhiteCenterAnalyzingResult, verticalLine: Pair<Point, Point>, horizontalLine: Pair<Point, Point>) {
    val classloader = Thread.currentThread().contextClassLoader
    val fileLocationFile = classloader.getResourceAsStream("debug_output_location.txt")
        ?: throw IllegalArgumentException("There is no debug_output_location in the resources folder")
    val debugProcessedFileLocation = fileLocationFile.bufferedReader().use { it.readText() } + System.currentTimeMillis() + ".jpg"
    // Check if the directory exists
    val directory = File(debugProcessedFileLocation.substringBeforeLast("/"))
    if (!directory.exists()) {
        throw IllegalArgumentException("Invalid directory: $debugProcessedFileLocation")
    }

    val sizeOfSign = floor(sqrt(sign.width() * sign.height().toDouble())).toInt()

    // Write extreme points to the image
    for (point in extremities.toArray()) {
        Imgproc.circle(sign, point, sizeOfSign / 40, Scalar(0.0, 0.0, 255.0), -1)
    }

    // Write total colors to the image
    var colorText = ""
    for (color in colorsTotalSign) {
        val percentage: String = (color.getShareOnSign() * 100).roundToInt().toString() + "%"
        colorText += color.getApproximatedColor().name + ": " + percentage + ", "
    }
    Imgproc.putText(sign, colorText, Point(100.0, 2000.0), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 0.0), 2)

    // Write position of the white center to the image
    Imgproc.putText(sign, whiteCenter.toString(), Point(100.0, 2200.0), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 0.0), 2)

    // Write the recognized shape and the horizontal and vertical line to the image
    Imgproc.putText(sign, recognizeShape(extremities).name, Point(100.0, 2400.0), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 0.0), 2)
    Imgproc.drawContours(sign, listOf(MatOfPoint(verticalLine.first, verticalLine.second),  MatOfPoint(horizontalLine.first, horizontalLine.second)), -1, Scalar(0.0, 255.0, 0.0), sizeOfSign / 100)

    Imgcodecs.imwrite(debugProcessedFileLocation, sign)
}

private fun writeCroppedSign(croppedSign: Mat) {
    val classloader = Thread.currentThread().contextClassLoader
    val fileLocationFile = classloader.getResourceAsStream("debug_output_location.txt")
        ?: throw IllegalArgumentException("There is no debug_output_location in the resources folder")
    val debugProcessedFileLocation = fileLocationFile.bufferedReader().use { it.readText() } + System.currentTimeMillis() + "_cropped" + ".jpg"
    // Check if the directory exists
    val directory = File(debugProcessedFileLocation.substringBeforeLast("/"))
    if (!directory.exists()) {
        throw IllegalArgumentException("Invalid directory: $debugProcessedFileLocation")
    }

    Imgcodecs.imwrite(debugProcessedFileLocation, croppedSign)
}

