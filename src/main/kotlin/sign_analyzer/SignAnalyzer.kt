package org.hszg.sign_analyzer

import analyzeColors
import analyzeColorsLeftRight
import cropSign
import org.hszg.SignLoading.LoadableSign
import org.hszg.sign_analyzer.line_finder.findVerticalLine
import org.hszg.sign_analyzer.shape_recognizer.recognizeShape
import org.hszg.sign_properties.SignColor
import org.hszg.sign_properties.SignProperties
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sqrt


@Throws(SignAnalysisException::class)
fun analyzeSign(loadableSign: LoadableSign, writeDebugImage : Boolean = false) : SignProperties {
    try {
        val sign = loadableSign.loadImage()
        val croppedSign = cropSign(sign)
        if (writeDebugImage) {
            writeCroppedSign(croppedSign)
        }

        val extremities = findExtremities(sign)
        val verticalLine = findVerticalLine(extremities)

        val colorsTotalSign = analyzeColors(croppedSign)
        val colorsLeftRight = analyzeColorsLeftRight(croppedSign, verticalLine)

        if (writeDebugImage) {
            writeDebugResultImage(
                croppedSign,
                extremities,
                colorsTotalSign,
                colorsLeftRight,
                verticalLine
            )
        }
        return SignProperties(
            colors = colorsTotalSign,
            shape = recognizeShape(extremities),
            colorsLeft = colorsLeftRight.first,
            colorsRight = colorsLeftRight.second
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
 * @param colorsLeftRight The colors of the left and right side of the sign. Will be written to the image as text.
 * @param verticalLine The vertical line of the sign. Will be drawn to the image as a green line.
 */
private fun writeDebugResultImage(sign: Mat, extremities: MatOfPoint, colorsTotalSign: List<SignColor>, colorsLeftRight: Pair<List<SignColor>, List<SignColor>>,  verticalLine: Pair<Point, Point>) {
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

    // Write left and right colors to the image
    var leftColorText = "Left Colors: "
    for (color in colorsLeftRight.first) {
        val percentage: String = (color.getShareOnSign() * 100).roundToInt().toString() + "%"
        leftColorText += color.getApproximatedColor().name + ": " + percentage + ", "
    }
    Imgproc.putText(sign, leftColorText, Point(100.0, 2100.0), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 0.0), 2)

    var rightColorText = "Right Colors: "
    for (color in colorsLeftRight.second) {
        val percentage: String = (color.getShareOnSign() * 100).roundToInt().toString() + "%"
        rightColorText += color.getApproximatedColor().name + ": " + percentage + ", "
    }
    Imgproc.putText(sign, rightColorText, Point(100.0, 2200.0), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 0.0), 2)

    // Write the recognized shape and the horizontal and vertical line to the image
    Imgproc.putText(sign, recognizeShape(extremities).name, Point(100.0, 2400.0), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 0.0), 2)
    Imgproc.drawContours(sign, listOf(MatOfPoint(verticalLine.first, verticalLine.second)), -1, Scalar(0.0, 255.0, 0.0), sizeOfSign / 100)

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

