package com.example.nutritiontracker.core.image

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileNotFoundException

data class CameraImageTarget(
    val uri: Uri,
    val filePath: String,
)

class ImageStorage(
    private val context: Context,
) {
    fun copyImageToPrivateStorage(sourceUri: Uri): String {
        val target = newImageFile(prefix = "picked")
        val input = context.contentResolver.openInputStream(sourceUri)
            ?: throw FileNotFoundException("Cannot open image uri: $sourceUri")
        input.use { source ->
            target.outputStream().use { destination ->
                source.copyTo(destination)
            }
        }
        return target.absolutePath
    }

    fun createCameraImageTarget(): CameraImageTarget {
        val target = newImageFile(prefix = "camera")
        return CameraImageTarget(
            uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", target),
            filePath = target.absolutePath,
        )
    }

    private fun newImageFile(prefix: String): File {
        val imagesDir = File(context.filesDir, "food-images")
        imagesDir.mkdirs()
        return File(imagesDir, "$prefix-${System.currentTimeMillis()}-${System.nanoTime()}.jpg")
    }
}
