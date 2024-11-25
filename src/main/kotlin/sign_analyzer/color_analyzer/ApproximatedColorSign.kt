package org.hszg.sign_analyzer.color_analyzer

import org.hszg.sign_properties.ApproximatedColor
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
/**
 * Represents a single pixel in a sign with its position (row and column) and the approximated color.
 */
class ApproximatedColorSignItem(
    private val row: Int,
    private val col: Int,
    private val color: ApproximatedColor
) {
    /**
     * Gets the row position of the pixel in the sign.
     *
     * @return The row index of the pixel.
     */
    fun getRow(): Int = row

    /**
     * Gets the column position of the pixel in the sign.
     *
     * @return The column index of the pixel.
     */
    fun getCol(): Int = col

    /**
     * Gets the approximated color of the pixel.
     *
     * @return The approximated color of the pixel as an ApproximatedColor.
     */
    fun getColor(): ApproximatedColor = color
}

/**
 * Represents a sign with a list of pixels, where each pixel has an approximated color and its position.
 */
class ApproximatedColorSign(private val pixels: List<ApproximatedColorSignItem>) {
    /**
     * Gets the list of pixels in the sign.
     * Each pixel is represented by an ApproximatedColorSignItem containing its position and color.
     *
     * @return A list of ApproximatedColorSignItem objects.
     */
    fun getPixels(): List<ApproximatedColorSignItem> = pixels

    /**
     * Calculates the dimensions of the sign based on the maximum row and column indices of the pixels.
     *
     * @return A Size object representing the width and height of the sign.
     */
    fun getSize(): Size {
        val rows = pixels.maxOfOrNull { it.getRow() } ?: 0
        val cols = pixels.maxOfOrNull { it.getCol() } ?: 0
        return Size(cols.toDouble(), rows.toDouble())
    }

    /**
     * Converts the sign into an OpenCV Mat object with RGBA color representation.
     * Each pixel is placed at its respective row and column in the Mat.
     *
     * @return A Mat object representing the sign with RGBA colors.
     */
    fun toRGBAMat(): Mat {
        val size = getSize()
        val mat = Mat.zeros(size, CvType.CV_8UC4)
        for (item in pixels) {
            val row = item.getRow()
            val col = item.getCol()
            val colorArray = item.getColor().getRGBAArray()
            mat.put(row, col, *colorArray)
        }
        return mat
    }
}
