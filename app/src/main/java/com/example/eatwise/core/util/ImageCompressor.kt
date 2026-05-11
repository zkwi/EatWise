package com.example.eatwise.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import com.example.eatwise.core.storage.ImageStorage
import java.io.File
import kotlin.math.min

class ImageCompressor(context: Context) {
    private val imageStorage = ImageStorage(context)

    fun compress(original: File): File {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(original.absolutePath, options)
        val sample = calculateSampleSize(options.outWidth, options.outHeight)
        val bitmap = BitmapFactory.Options().run {
            inSampleSize = sample
            BitmapFactory.decodeFile(original.absolutePath, this)
        } ?: error("图片处理失败，请重试。")

        var fixed: Bitmap? = null
        var scaled: Bitmap? = null
        try {
            fixed = bitmap.fixOrientation(original)
            scaled = fixed.scaleToFit(MAX_UPLOAD_WIDTH, MAX_UPLOAD_HEIGHT)
            val target = imageStorage.compressedFileFor(original)
            target.outputStream().use { output ->
                scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            }
            return target
        } finally {
            if (scaled != null && scaled !== fixed) scaled.recycleIfNeeded()
            if (fixed != null && fixed !== bitmap) fixed.recycleIfNeeded()
            bitmap.recycleIfNeeded()
        }
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        if (width <= 0 || height <= 0) return 1
        var sample = 1
        while (width / sample > MAX_UPLOAD_WIDTH * 2 || height / sample > MAX_UPLOAD_HEIGHT * 2) {
            sample *= 2
        }
        return sample
    }

    private fun Bitmap.scaleToFit(maxWidth: Int, maxHeight: Int): Bitmap {
        if (width <= maxWidth && height <= maxHeight) return this
        val ratio = min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        return scale(
            (width * ratio).toInt().coerceAtLeast(1),
            (height * ratio).toInt().coerceAtLeast(1),
        )
    }

    private fun Bitmap.fixOrientation(file: File): Bitmap {
        val orientation = runCatching {
            ExifInterface(file.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val rotation = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (rotation == 0f) return this
        return Bitmap.createBitmap(this, 0, 0, width, height, Matrix().apply { postRotate(rotation) }, true)
    }

    private fun Bitmap.recycleIfNeeded() {
        if (!isRecycled) recycle()
    }

    private companion object {
        const val MAX_UPLOAD_WIDTH = 1600
        const val MAX_UPLOAD_HEIGHT = 1600
        const val JPEG_QUALITY = 85
    }
}
