package com.mascotasperdidas.app.data.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Compresses raw image bytes to a downscaled JPEG.
 * Android-graphics imports are intentionally here in [data/] — NOT in [domain/].
 * Injectable and mockable so repository orchestration tests never need a real Bitmap.
 */
@Singleton
class ImageCompressor @Inject constructor() {

    /**
     * Decodes [raw] bytes, downscales so that the longest side is at most [maxDim] px
     * (never upscales), then compresses to JPEG with [quality] (0-100).
     *
     * @throws IllegalArgumentException if [raw] is empty or cannot be decoded as a bitmap.
     */
    fun compressToJpeg(
        raw: ByteArray,
        maxDim: Int = 1024,
        quality: Int = 70,
    ): ByteArray {
        require(raw.isNotEmpty()) { "empty image bytes" }

        val original = BitmapFactory.decodeByteArray(raw, 0, raw.size)
            ?: throw IllegalArgumentException("Failed to decode image bytes")

        val scale = (maxDim.toFloat() / max(original.width, original.height)).coerceAtMost(1.0f)
        val newW = (original.width * scale).toInt().coerceAtLeast(1)
        val newH = (original.height * scale).toInt().coerceAtLeast(1)

        val scaled = Bitmap.createScaledBitmap(original, newW, newH, true)

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)

        if (scaled !== original) scaled.recycle()
        original.recycle()

        return out.toByteArray()
    }
}
