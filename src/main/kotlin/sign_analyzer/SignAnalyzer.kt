package org.hszg.sign_analyzer

import analyzeColors
import org.hszg.sign_cropper.cropSignWithTransparency
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import kotlin.math.roundToInt


fun analyzeSign(sign: Mat, debugProcessedFileLocation: String?) {
    val croppedSign = cropSignWithTransparency(sign)
    val colors = analyzeColors(croppedSign)
    val extremities = findExtremities(sign)

    if (debugProcessedFileLocation != null) {
        // Check if the directory exists
        val directory = File(debugProcessedFileLocation.substringBeforeLast("/"))
        if (!directory.exists()) {
            throw IllegalArgumentException("Invalid directory: $debugProcessedFileLocation")
        }

        // Write extreme points to the image
        for (point in extremities.toArray()) {
            Imgproc.circle(sign, point, 50, Scalar(0.0, 0.0, 255.0), -1)
        }

        var colorText = ""
        for (color in colors) {
            val percentage: String = (color.getShareOnTotalSign() * 100).roundToInt().toString() + "%"
            colorText += color.getApproximatedColor().name + ": " + percentage + ", "
        }
        Imgproc.putText(sign, colorText, Point(100.0, 2000.0), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 0.0), 2);

        Imgcodecs.imwrite(debugProcessedFileLocation, sign)
    }
}

