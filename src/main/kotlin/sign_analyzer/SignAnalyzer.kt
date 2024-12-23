package org.hszg.sign_analyzer

import analyzeColors
import cropSign
import getSignWithApproximatedColor
import org.hszg.SignLoading.LoadableSign
import org.hszg.sign_analyzer.center_symbol_analyzer.CenterSymbolAnalyzerResult
import org.hszg.sign_analyzer.extremities_finder.findCorners
import org.hszg.sign_properties.SignColor
import org.hszg.sign_properties.SignProperties
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import kotlin.math.*

/**
 * Analyze the sign and return the properties of the sign.
 * The properties include the colors of the sign, the number of corners the center of the white pixels.
 * @param loadableSign The sign to analyze.
 * @param writeDebugImage If true, a debug image will be written to the directory specified in the resources folder.
 * @return The properties of the sign.
 */

fun analyzeSign(loadableSign: LoadableSign, writeDebugImage : Boolean = false) : SignProperties {
        val sign = loadableSign.loadImage()

        val croppedSignWithContour = cropSign(sign)
        val croppedSign = croppedSignWithContour.first
        val croppingContour = croppedSignWithContour.second

        // Corners are the extreme points of the sign. They give a rough idea of the shape of the sign. There are arbitrary in number.
        val corners = findCorners(croppingContour)

        val colors = analyzeColors(croppedSign)
        val colorsTotalSign = colors.first
        val approximatedColorSign = colors.second
        val centerSymbol = analyzeCenterSymbol(approximatedColorSign, colorsTotalSign, croppingContour)

        val cornersCountNormalizer = Math.clamp(corners.toList().size.toDouble() / 9, 0.0, 1.0)

        if (writeDebugImage) {
            writeDebugResultImage(
                sign,
                croppingContour,
                corners,
                colorsTotalSign,
                centerSymbol
            )
        }
        return SignProperties(
            colors = colorsTotalSign,
            cornersCountNormalized = cornersCountNormalizer,
            centerSymbolAnalyzerResult = centerSymbol
        )
}

/**
 * Write the debug image with the result of the sign analysis.
 * The image will be written to the directory specified in the resources folder.
 * @param sign The sign image. Will be the base for the debug image..
 * @param colorsTotalSign The colors of the sign. Will be written to the image as text.
 * @param centerSymbolAnalyzerResult The result of the center symbol analysis. Will be written to the image as text.
 */
private fun writeDebugResultImage(
    sign: Mat,
    croppingContour: MatOfPoint,
    corners: List<Point>,
    colorsTotalSign: List<SignColor>,
    centerSymbolAnalyzerResult: CenterSymbolAnalyzerResult
) {
    val classloader = Thread.currentThread().contextClassLoader
    val fileLocationFile = classloader.getResourceAsStream("debug_output_location.txt")
        ?: throw IllegalArgumentException("There is no debug_output_location in the resources folder")
    val debugProcessedFileLocation = fileLocationFile.bufferedReader().use { it.readText() } + System.currentTimeMillis() + ".jpg"

    // Check if the directory exists
    val directory = File(debugProcessedFileLocation.substringBeforeLast("/"))
    if (!directory.exists()) {
        throw IllegalArgumentException("Invalid directory: $debugProcessedFileLocation")
    }

    val signSize = sqrt(sign.rows().toDouble().pow(2) + sign.cols().toDouble().pow(2))

    // Write corners to image
    for (point in corners) {
        Imgproc.circle(sign, point,  ceil(signSize / 20).toInt(), Scalar(0.0, 0.0, 255.0), -1)
    }

    // Write contour to image
    val contour = croppingContour.toList()
    for (i in 0 until contour.size) {
        Imgproc.line(sign, contour[i], contour[(i + 1) % contour.size], Scalar(0.0, 255.0, 0.0), ceil(signSize / 40).toInt())
    }

    // Resize the image to a fixed size to make the analysis more consistent
    // Done after writing the corners and contour to make sure they are in their original position
    Imgproc.resize(sign, sign, Size(2000.0, 2000.0))

    // Write debug text to the image
    var colorText = ""
    for (color in colorsTotalSign) {
        val percentage: String = (color.getShareOnSign() * 100).roundToInt().toString() + "%"
        colorText += color.getApproximatedColor().name + ": " + percentage + ", "
    }
    val centerSymbolAnalyzerResultText = "Center symbol: $centerSymbolAnalyzerResult"
    val cornersText = "Corners: " + corners.toList().size
    writeTextForDebugImage(sign, listOf(cornersText, colorText, centerSymbolAnalyzerResultText), Point(100.0, 200.0))

    // Concert to BGR for writing
    Imgproc.cvtColor(sign, sign, Imgproc.COLOR_RGBA2BGR)
    Imgcodecs.imwrite(debugProcessedFileLocation, sign)
}

private fun writeTextForDebugImage(
    sign: Mat,
    texts: List<String>,
    startingPosition: Point,
    font: Int = Imgproc.FONT_HERSHEY_SIMPLEX,
    fontScale: Double = 2.0,
    fontThickness: Int = 2,
    fontColor: Scalar = Scalar(255.0, 255.0, 0.0)
) {
    for ((index, text) in texts.withIndex()) {
        Imgproc.putText(sign, text, Point(startingPosition.x, startingPosition.y + index * 200), font, fontScale, fontColor, fontThickness)
    }
}
