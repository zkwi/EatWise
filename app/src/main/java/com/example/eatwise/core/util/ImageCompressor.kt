package com.example.eatwise.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.example.eatwise.core.storage.ImageStorage
import java.io.File
import kotlin.math.max

class ImageCompressor(context: Context) {
    private val imageStorage = ImageStorage(context)

    fun compress(original: File): File {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(original.absolutePath, options)
        val sample = calculateSampleSize(options.outWidth, options.outHeight, 1280)
        val bitmap = BitmapFactory.Options().run {
            inSampleSize = sample
            BitmapFactory.decodeFile(original.absolutePath, this)
        } ?: error("图片处理失败，请重试。")

        val fixed = bitmap.fixOrientation(original)
        val scaled = fixed.scaleToMaxSide(1280)
        val target = imageStorage.compressedFileFor(original)
        target.outputStream().use { output ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, output)
        }
        if (fixed !== bitmap) bitmap.recycle()
        if (scaled !== fixed) fixed.recycle()
        return target
    }

    private fun calculateSampleSize(width: Int, height: Int, maxSide: Int): Int {
        var sample = 1
        var largest = max(width, height)
        while (largest / sample > maxSide * 2) sample *= 2
        return sample
    }

    private fun Bitmap.scaleToMaxSide(maxSide: Int): Bitmap {
        val largest = max(width, height)
        if (largest <= maxSide) return this
        val ratio = maxSide.toFloat() / largest
        return Bitmap.createScaledBitmap(this, (width * ratio).toInt(), (height * ratio).toInt(), true)
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
}
