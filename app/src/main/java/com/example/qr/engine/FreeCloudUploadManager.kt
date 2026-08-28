package com.example.qr.engine

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Catbox / Litterbox Cloud Document Uploader
 *
 * Exclusively uses Catbox.moe & Litterbox for fast, free, direct file hosting:
 * 1. Primary: Litterbox (Temporary 72-hour direct file hosting - https://litter.catbox.moe/...)
 * 2. Fallback: Catbox.moe (Direct file hosting - https://files.catbox.moe/...)
 *
 * Guarantees:
 * - 100% Free (no credit card or Blaze plan needed)
 * - Safe & direct file links with no intermediate landing pages, popup ads, or APK prompts
 * - Strict adherence to Catbox / Litterbox endpoints only
 */
class FreeCloudUploadManager(private val context: Context) {

    private val tag = "FreeCloudUploadManager"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
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

    suspend fun uploadFile(
        uri: Uri,
        onProgress: (Int) -> Unit = {}
    ): Result<CloudUploadResult> = withContext(Dispatchers.IO) {
        val (displayName, declaredSize) = getFileNameAndSize(uri)
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

        onProgress(10)

        // Copy content URI to temporary cache file
        val tempFile = File(context.cacheDir, "catbox_${System.currentTimeMillis()}_${displayName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            return@withContext Result.failure(IOException("Failed to read file from phone: ${e.localizedMessage}"))
        }

        val actualSize = if (tempFile.length() > 0) tempFile.length() else declaredSize
        onProgress(30)

        // 1. Primary: Upload to Litterbox (Catbox 72h temporary hosting)
        var lastError: String? = null
        try {
            onProgress(50)
            val litterboxResult = uploadToLitterbox(tempFile, displayName, mimeType, actualSize)
            if (litterboxResult != null) {
                onProgress(100)
                tempFile.delete()
                return@withContext Result.success(litterboxResult)
            }
        } catch (e: Exception) {
            lastError = e.message
            Log.w(tag, "Litterbox upload error: ${e.message}, trying Catbox fallback")
        }

        // 2. Secondary: Fallback to Catbox.moe main upload service
        try {
            onProgress(75)
            val catboxResult = uploadToCatbox(tempFile, displayName, mimeType, actualSize)
            if (catboxResult != null) {
                onProgress(100)
                tempFile.delete()
                return@withContext Result.success(catboxResult)
            }
        } catch (e: Exception) {
            lastError = e.message
            Log.e(tag, "Catbox upload error: ${e.message}")
        }

        tempFile.delete()
        val errorDetail = if (!lastError.isNullOrBlank()) ": $lastError" else ""
        Result.failure(IOException("Catbox/Litterbox upload failed$errorDetail. Please verify your internet connection and try again."))
    }

    /**
     * Litterbox (by Catbox.moe): 72-hour temporary direct file hosting.
     * Generates instant direct URLs (e.g., https://litter.catbox.moe/abc.pdf)
     */
    private fun uploadToLitterbox(file: File, fileName: String, mimeType: String, fileSize: Long): CloudUploadResult? {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("reqtype", "fileupload")
            .addFormDataPart("time", "72h") // 72 hours retention
            .addFormDataPart(
                "fileToUpload",
                fileName,
                file.asRequestBody(mimeType.toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("https://litterbox.catbox.moe/resources/internals/api.php")
            .post(requestBody)
            .addHeader("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0) Gecko/120.0 Firefox/120.0")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()?.trim() ?: ""

        if (response.isSuccessful && responseBody.startsWith("http", ignoreCase = true)) {
            return CloudUploadResult(
                fileName = fileName,
                fileSizeBytes = fileSize,
                mimeType = mimeType,
                publicUrl = responseBody,
                directDownloadUrl = responseBody,
                expiresAt = System.currentTimeMillis() + (72L * 60 * 60 * 1000L),
                serviceName = "Litterbox (Catbox)"
            )
        } else {
            Log.w(tag, "Litterbox non-URL response: $responseBody")
        }
        return null
    }

    /**
     * Catbox.moe: Official Catbox upload endpoint.
     * Generates direct URLs (e.g., https://files.catbox.moe/xyz.pdf)
     */
    private fun uploadToCatbox(file: File, fileName: String, mimeType: String, fileSize: Long): CloudUploadResult? {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("reqtype", "fileupload")
            .addFormDataPart(
                "fileToUpload",
                fileName,
                file.asRequestBody(mimeType.toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("https://catbox.moe/user/api.php")
            .post(requestBody)
            .addHeader("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0) Gecko/120.0 Firefox/120.0")
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()?.trim() ?: ""

        if (response.isSuccessful && responseBody.startsWith("http", ignoreCase = true)) {
            return CloudUploadResult(
                fileName = fileName,
                fileSizeBytes = fileSize,
                mimeType = mimeType,
                publicUrl = responseBody,
                directDownloadUrl = responseBody,
                expiresAt = null, // Catbox standard files are persistent
                serviceName = "Catbox.moe"
            )
        } else {
            Log.w(tag, "Catbox non-URL response: $responseBody")
        }
        return null
    }
}
