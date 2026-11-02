package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.model.CapturedPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object PhotoStorageHelper {

    suspend fun saveBitmap(
        context: Context,
        bitmap: Bitmap,
        zoomRatio: Float
    ): CapturedPhoto? = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val timeString = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(timestamp))
        val fileName = "LENTE_${timeString}.jpg"

        // Save to internal app pictures folder for quick local gallery access
        val appDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "LenteIngrandimento").apply {
            if (!exists()) mkdirs()
        }
        val localFile = File(appDir, fileName)

        try {
            FileOutputStream(localFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            // Also insert into MediaStore for device Gallery visibility
            val mediaUri = saveToMediaStore(context, bitmap, fileName)
            val finalUri = mediaUri ?: Uri.fromFile(localFile)

            CapturedPhoto(
                id = UUID.randomUUID().toString(),
                uri = finalUri,
                filePath = localFile.absolutePath,
                timestamp = timestamp,
                zoomRatio = zoomRatio
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveToMediaStore(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/LenteIngrandimento")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null
        try {
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            return uri
        } catch (e: Exception) {
            e.printStackTrace()
            resolver.delete(uri, null, null)
            return null
        }
    }

    suspend fun loadSavedPhotos(context: Context): List<CapturedPhoto> = withContext(Dispatchers.IO) {
        val appDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "LenteIngrandimento")
        if (!appDir.exists()) return@withContext emptyList()

        val files = appDir.listFiles { file -> file.extension.lowercase() in listOf("jpg", "jpeg", "png") }
            ?.sortedByDescending { it.lastModified() }
            ?: return@withContext emptyList()

        files.map { file ->
            CapturedPhoto(
                id = file.name,
                uri = Uri.fromFile(file),
                filePath = file.absolutePath,
                timestamp = file.lastModified(),
                zoomRatio = 1.0f
            )
        }
    }

    suspend fun deletePhoto(context: Context, photo: CapturedPhoto): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(photo.filePath)
            val deleted = if (file.exists()) file.delete() else false
            if (photo.uri.scheme == "content") {
                try {
                    context.contentResolver.delete(photo.uri, null, null)
                } catch (_: Exception) {}
            }
            deleted
        } catch (e: Exception) {
            false
        }
    }

    fun sharePhoto(context: Context, photo: CapturedPhoto) {
        try {
            val file = File(photo.filePath)
            val shareUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                } catch (_: Exception) {
                    photo.uri
                }
            } else {
                Uri.fromFile(file)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Condividi foto ingrandita"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
