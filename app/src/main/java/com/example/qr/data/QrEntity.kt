package com.example.qr.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_codes")
data class QrEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: String, // TEXT, URL, WIFI, CONTACT, EMAIL, PHONE, SMS, DOCUMENT, CALENDAR, LOCATION, DYNAMIC
    val rawInput: String,
    val payload: String,
    val fgColor: Int = 0xFF0F172A.toInt(),
    val bgColor: Int = 0xFFFFFFFF.toInt(),
    val isGradient: Boolean = false,
    val gradientColorEnd: Int = 0xFF2563EB.toInt(),
    val dotStyle: String = "SQUARE",
    val cornerStyle: String = "ROUNDED",
    val errorCorrection: String = "HIGH",
    val logoBadge: String = "NONE",
    val customLogoUri: String? = null,
    val isDynamic: Boolean = false,
    val dynamicId: String? = null,
    val dynamicTargetUrl: String? = null,
    val scanCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val tags: String = ""
)
