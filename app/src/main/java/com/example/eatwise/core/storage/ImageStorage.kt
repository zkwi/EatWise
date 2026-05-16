package com.example.eatwise.core.storage

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

class ImageStorage(
    private val context: Context,
) {
    private val imagesDir = File(context.filesDir, "images")
    private val originalDir = File(imagesDir, "original")
    private val compressedDir = File(imagesDir, "compressed")

    init {
        originalDir.mkdirs()
        compressedDir.mkdirs()
    }

    fun copyToPrivateStorage(uri: Uri): File {
        val target = File(originalDir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: error("图片读取失败，请换一张图片。")
        return target
    }

    fun copyResourceToPrivateStorage(resourceId: Int, name: String): File {
        val safeName = name.lowercase().replace(Regex("[^a-z0-9_]+"), "_").trim('_')
        val target = File(originalDir, "${safeName}_${UUID.randomUUID()}.jpg")
        context.resources.openRawResource(resourceId).use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return target
    }

    fun createCameraImageFile(): File {
        originalDir.mkdirs()
        return File(originalDir, "${UUID.randomUUID()}.jpg")
    }

    fun compressedFileFor(original: File): File {
        compressedDir.mkdirs()
        return File(compressedDir, "${original.nameWithoutExtension}_${UUID.randomUUID()}_compressed.jpg")
    }

    fun deleteImageFiles(vararg paths: String?) {
        paths.filterNotNull().forEach { runCatching { File(it).delete() } }
    }
}
