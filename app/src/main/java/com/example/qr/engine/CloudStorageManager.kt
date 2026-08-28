package com.example.qr.engine

data class CloudUploadResult(
    val fileName: String,
    val fileSizeBytes: Long,
    val mimeType: String,
    val publicUrl: String,
    val directDownloadUrl: String,
    val expiresAt: Long? = null,
    val serviceName: String = "Firebase Cloud Storage"
)

sealed class CloudUploadState {
    object Idle : CloudUploadState()
    data class Uploading(val progressPercent: Int, val fileName: String) : CloudUploadState()
    data class Success(val result: CloudUploadResult) : CloudUploadState()
    data class Error(val message: String) : CloudUploadState()
}

