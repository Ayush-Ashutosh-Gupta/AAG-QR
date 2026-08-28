package com.example.qr.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hosted_files")
data class HostedFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val fileSizeBytes: Long,
    val fileMimeType: String,
    val downloadUrl: String,
    val deleteKey: String? = null,
    val uploadedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000L), // 24 Hours TTL
    val status: String = "ACTIVE", // ACTIVE, EXPIRED, DELETED
    val localUriString: String? = null
)
