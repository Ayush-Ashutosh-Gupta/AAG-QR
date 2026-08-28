package com.example.qr.firebase

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.qr.engine.CloudUploadResult
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class FirebaseStorageManager(private val context: Context) {

    private val tag = "FirebaseStorageManager"

    private fun getStorageInstance(bucketUrl: String? = null): FirebaseStorage {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        return if (!bucketUrl.isNullOrBlank()) {
            val formattedUrl = if (bucketUrl.startsWith("gs://")) bucketUrl else "gs://$bucketUrl"
            FirebaseStorage.getInstance(formattedUrl)
        } else {
            FirebaseStorage.getInstance()
        }
    }

    fun getFileNameAndSize(uri: Uri): Pair<String, Long> {
        var name = "Document_${System.currentTimeMillis()}"
        var size = 0L
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        name = it.getString(nameIndex) ?: name
                    }
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        size = it.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {}
        return Pair(name, size)
    }

    suspend fun uploadToFirebaseStorage(
        userId: String,
        uri: Uri,
        onProgress: (Int) -> Unit = {}
    ): Result<CloudUploadResult> = withContext(Dispatchers.IO) {
        val (displayName, declaredSize) = getFileNameAndSize(uri)
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

        // 1. Copy stream to cache file for clean upload
        val tempFile = File(context.cacheDir, "fb_upload_${System.currentTimeMillis()}_${displayName.replace(" ", "_")}")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(IOException("Unable to read selected file."))
        } catch (e: Exception) {
            return@withContext Result.failure(IOException("Failed to read file from storage: ${e.message}"))
        }

        val actualSize = if (tempFile.length() > 0) tempFile.length() else declaredSize
        val sanitizedName = displayName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val uniquePrefix = UUID.randomUUID().toString().take(8)
        val storagePath = "documents/${userId.ifBlank { "public" }}/${uniquePrefix}_$sanitizedName"

        val metadata = StorageMetadata.Builder()
            .setContentType(mimeType)
            .setCustomMetadata("originalName", displayName)
            .setCustomMetadata("uploadedAt", System.currentTimeMillis().toString())
            .build()

        // List of candidate bucket references to try
        val candidateBuckets = listOf(
            null, // Default bucket from google-services.json
            "aag-qr.firebasestorage.app",
            "aag-qr.appspot.com"
        )

        var lastException: Exception? = null

        for (bucket in candidateBuckets) {
            try {
                val storage = getStorageInstance(bucket)
                val fileRef = storage.reference.child(storagePath)

                val uploadTask = fileRef.putFile(Uri.fromFile(tempFile), metadata)

                uploadTask.addOnProgressListener { taskSnapshot ->
                    val total = taskSnapshot.totalByteCount
                    val transferred = taskSnapshot.bytesTransferred
                    if (total > 0) {
                        val pct = ((transferred * 100) / total).toInt().coerceIn(0, 99)
                        onProgress(pct)
                    }
                }

                uploadTask.await()
                val downloadUrl = fileRef.downloadUrl.await().toString()
                onProgress(100)
                tempFile.delete()

                return@withContext Result.success(
                    CloudUploadResult(
                        fileName = displayName,
                        fileSizeBytes = actualSize,
                        mimeType = mimeType,
                        publicUrl = downloadUrl,
                        directDownloadUrl = downloadUrl,
                        expiresAt = null,
                        serviceName = "Firebase Cloud Storage"
                    )
                )
            } catch (e: Exception) {
                lastException = e
                Log.w(tag, "Upload attempt with bucket '$bucket' failed: ${e.message}")
            }
        }

        tempFile.delete()
        
        // Diagnose specific error
        val userFriendlyMessage = when {
            lastException is StorageException && (lastException.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND || lastException.message?.contains("does not exist", ignoreCase = true) == true) -> {
                "Firebase Storage bucket is not enabled or does not exist yet. Please go to Firebase Console (https://console.firebase.google.com) > Build > Storage > click 'Get Started' to activate Cloud Storage, and check Storage Rules."
            }
            lastException is StorageException && lastException.errorCode == StorageException.ERROR_NOT_AUTHORIZED -> {
                "Firebase Storage Permission Denied. Please check Firebase Console > Storage > Rules tab (e.g., set 'allow read, write: if true;' for documents testing)."
            }
            else -> {
                "Firebase upload failed: ${lastException?.localizedMessage ?: lastException?.message ?: "Unknown error"}"
            }
        }

        Log.e(tag, "Firebase Storage upload error: $userFriendlyMessage", lastException)
        Result.failure(IOException(userFriendlyMessage))
    }
}


