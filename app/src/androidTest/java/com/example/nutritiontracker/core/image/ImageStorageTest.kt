package com.example.nutritiontracker.core.image

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import java.io.File
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageStorageTest {
    @Test
    fun copyImageToPrivateStorageCopiesReadableContent() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val source = File(context.cacheDir, "source-image.jpg").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
        }
        val storage = ImageStorage(context)

        val copiedPath = storage.copyImageToPrivateStorage(Uri.fromFile(source))

        val copied = File(copiedPath)
        assertTrue(copied.exists())
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), copied.readBytes())
    }

    @Test
    fun cameraTargetCreatesWritableUriAndPrivatePath() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val storage = ImageStorage(context)

        val target = storage.createCameraImageTarget()
        context.contentResolver.openOutputStream(target.uri)!!.use { output ->
            output.write(byteArrayOf(9, 8, 7))
        }

        val file = File(target.filePath)
        assertTrue(file.exists())
        assertArrayEquals(byteArrayOf(9, 8, 7), file.readBytes())
    }
}
