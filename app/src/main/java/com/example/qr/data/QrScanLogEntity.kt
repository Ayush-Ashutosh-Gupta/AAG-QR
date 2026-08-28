package com.example.qr.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "qr_scan_logs",
    foreignKeys = [
        ForeignKey(
            entity = QrEntity::class,
            parentColumns = ["id"],
            childColumns = ["qrId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("qrId")]
)
data class QrScanLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val qrId: Long,
    val scannedAt: Long = System.currentTimeMillis(),
    val locationCity: String = "Local Device",
    val locationCountry: String = "Offline",
    val platform: String = "Android",
    val notes: String = ""
)
