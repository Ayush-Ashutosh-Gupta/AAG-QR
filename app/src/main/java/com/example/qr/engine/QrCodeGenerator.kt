package com.example.qr.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object QrCodeGenerator {

    fun generateQrBitmap(
        content: String,
        size: Int = 800,
        customization: QrCustomization = QrCustomization(),
        context: Context? = null
    ): Bitmap? {
        if (content.isBlank()) return null
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 2)
                val zxingEcLevel = when (customization.errorCorrection) {
                    ErrorCorrection.LOW -> ErrorCorrectionLevel.L
                    ErrorCorrection.MEDIUM -> ErrorCorrectionLevel.M
                    ErrorCorrection.QUARTILE -> ErrorCorrectionLevel.Q
                    ErrorCorrection.HIGH -> ErrorCorrectionLevel.H
                }
                put(EncodeHintType.ERROR_CORRECTION, zxingEcLevel)
            }

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Draw Background
            val bgPaint = Paint().apply {
                color = customization.backgroundColor
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

            // Calculate Module Size
            val moduleWidth = size.toFloat() / matrixWidth.toFloat()
            val moduleHeight = size.toFloat() / matrixHeight.toFloat()

            // Foreground Paint with optional Gradient
            val fgPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                if (customization.isGradient) {
                    shader = LinearGradient(
                        0f, 0f, size.toFloat(), size.toFloat(),
                        customization.foregroundColor,
                        customization.gradientColorEnd,
                        Shader.TileMode.CLAMP
                    )
                } else {
                    color = customization.foregroundColor
                }
            }

            // Finder Pattern Boundaries (7x7 modules at 3 corners)
            val margin = 2
            val finderTopLeft = RectF(0f, 0f, 9f * moduleWidth, 9f * moduleHeight)
            val finderTopRight = RectF((matrixWidth - 9f) * moduleWidth, 0f, size.toFloat(), 9f * moduleHeight)
            val finderBottomLeft = RectF(0f, (matrixHeight - 9f) * moduleHeight, 9f * moduleWidth, size.toFloat())

            // Center Logo safe-zone boundary
            val centerLogoRadius = if (customization.logoBadge != LogoBadge.NONE) size * 0.13f else 0f
            val centerX = size / 2f
            val centerY = size / 2f

            for (x in 0 until matrixWidth) {
                for (y in 0 until matrixHeight) {
                    if (bitMatrix[x, y]) {
                        val left = x * moduleWidth
                        val top = y * moduleHeight
                        val right = left + moduleWidth
                        val bottom = top + moduleHeight
                        val rect = RectF(left, top, right, bottom)

                        // Check if module is in Finder Pattern
                        val isFinder = isInsideFinderPattern(x, y, matrixWidth, matrixHeight)

                        // If center logo is present, don't draw dots covered by center logo
                        if (centerLogoRadius > 0) {
                            val dist = Math.hypot((left + moduleWidth / 2 - centerX).toDouble(), (top + moduleHeight / 2 - centerY).toDouble())
                            if (dist < centerLogoRadius * 1.05) {
                                continue
                            }
                        }

                        if (isFinder) {
                            // Render Finder patterns according to cornerStyle
                            drawFinderModule(canvas, rect, customization.cornerStyle, fgPaint, moduleWidth)
                        } else {
                            // Render regular data module according to dotStyle
                            drawDataModule(canvas, rect, customization.dotStyle, fgPaint, moduleWidth)
                        }
                    }
                }
            }

            // Draw center logo if configured
            if (customization.logoBadge != LogoBadge.NONE && context != null) {
                drawCenterLogo(canvas, size, customization, context)
            }

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isInsideFinderPattern(x: Int, y: Int, width: Int, height: Int): Boolean {
        // Top-left finder (7x7 + 1 margin = [0..7])
        if (x in 0..7 && y in 0..7) return true
        // Top-right finder
        if (x in (width - 8) until width && y in 0..7) return true
        // Bottom-left finder
        if (x in 0..7 && y in (height - 8) until height) return true
        return false
    }

    private fun drawFinderModule(
        canvas: Canvas,
        rect: RectF,
        cornerStyle: CornerStyle,
        paint: Paint,
        moduleSize: Float
    ) {
        when (cornerStyle) {
            CornerStyle.SQUARE -> {
                canvas.drawRect(rect, paint)
            }
            CornerStyle.ROUNDED -> {
                val cornerRadius = moduleSize * 0.35f
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
            }
            CornerStyle.CIRCLE -> {
                val radius = (rect.width() / 2f) * 0.95f
                canvas.drawCircle(rect.centerX(), rect.centerY(), radius, paint)
            }
        }
    }

    private fun drawDataModule(
        canvas: Canvas,
        rect: RectF,
        dotStyle: DotStyle,
        paint: Paint,
        moduleSize: Float
    ) {
        when (dotStyle) {
            DotStyle.SQUARE -> {
                canvas.drawRect(rect, paint)
            }
            DotStyle.ROUNDED -> {
                val radius = moduleSize * 0.38f
                canvas.drawRoundRect(rect, radius, radius, paint)
            }
            DotStyle.CIRCLE -> {
                val radius = (moduleSize / 2f) * 0.85f
                canvas.drawCircle(rect.centerX(), rect.centerY(), radius, paint)
            }
            DotStyle.SMOOTH -> {
                val radius = moduleSize * 0.48f
                canvas.drawRoundRect(rect, radius, radius, paint)
            }
        }
    }

    private fun drawCenterLogo(
        canvas: Canvas,
        size: Int,
        customization: QrCustomization,
        context: Context
    ) {
        val logoSize = (size * 0.22f).toInt()
        val cx = size / 2f
        val cy = size / 2f
        val radius = logoSize / 2f

        // Draw protective background pill/circle
        val bgBufferPaint = Paint().apply {
            color = customization.backgroundColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = customization.foregroundColor
            style = Paint.Style.STROKE
            strokeWidth = size * 0.008f
            isAntiAlias = true
        }

        canvas.drawCircle(cx, cy, radius + (size * 0.015f), bgBufferPaint)
        canvas.drawCircle(cx, cy, radius + (size * 0.015f), borderPaint)

        // Load custom or preset bitmap logo
        val logoBitmap = getLogoBitmap(customization, logoSize, context)
        if (logoBitmap != null) {
            val roundedLogo = getCircularBitmap(logoBitmap)
            val left = cx - (roundedLogo.width / 2f)
            val top = cy - (roundedLogo.height / 2f)
            canvas.drawBitmap(roundedLogo, left, top, null)
        }
    }

    private fun getLogoBitmap(
        customization: QrCustomization,
        targetSize: Int,
        context: Context
    ): Bitmap? {
        try {
            if (customization.logoBadge == LogoBadge.CUSTOM && customization.customLogoUri != null) {
                val uri = Uri.parse(customization.customLogoUri)
                val raw = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                return Bitmap.createScaledBitmap(raw, targetSize, targetSize, true)
            }

            // Draw vector icon badge onto bitmap
            val iconBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
            val iconCanvas = Canvas(iconBitmap)
            val iconPaint = Paint().apply {
                color = customization.foregroundColor
                isAntiAlias = true
                style = Paint.Style.FILL
            }

            // Draw representative geometry for built-in badges
            val pad = targetSize * 0.2f
            when (customization.logoBadge) {
                LogoBadge.SHIELD -> {
                    val p = android.graphics.Path().apply {
                        moveTo(targetSize / 2f, pad)
                        lineTo(targetSize - pad, pad * 1.4f)
                        lineTo(targetSize - pad, targetSize * 0.6f)
                        quadTo(targetSize / 2f, targetSize - pad, targetSize / 2f, targetSize - pad)
                        quadTo(pad, targetSize * 0.6f, pad, targetSize * 0.6f)
                        lineTo(pad, pad * 1.4f)
                        close()
                    }
                    iconCanvas.drawPath(p, iconPaint)
                }
                LogoBadge.WIFI -> {
                    val p = Paint(iconPaint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = targetSize * 0.09f
                        strokeCap = Paint.Cap.ROUND
                    }
                    iconCanvas.drawCircle(targetSize / 2f, targetSize * 0.72f, targetSize * 0.05f, iconPaint)
                    val arcRect1 = RectF(targetSize * 0.32f, targetSize * 0.44f, targetSize * 0.68f, targetSize * 0.8f)
                    iconCanvas.drawArc(arcRect1, 200f, 140f, false, p)
                    val arcRect2 = RectF(targetSize * 0.2f, targetSize * 0.28f, targetSize * 0.8f, targetSize * 0.88f)
                    iconCanvas.drawArc(arcRect2, 200f, 140f, false, p)
                }
                LogoBadge.LINK -> {
                    val p = Paint(iconPaint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = targetSize * 0.1f
                        strokeCap = Paint.Cap.ROUND
                    }
                    val oval1 = RectF(pad, pad, targetSize * 0.65f, targetSize * 0.65f)
                    iconCanvas.drawRoundRect(oval1, targetSize * 0.15f, targetSize * 0.15f, p)
                    val oval2 = RectF(targetSize * 0.35f, targetSize * 0.35f, targetSize - pad, targetSize - pad)
                    iconCanvas.drawRoundRect(oval2, targetSize * 0.15f, targetSize * 0.15f, p)
                }
                LogoBadge.CONTACT -> {
                    iconCanvas.drawCircle(targetSize / 2f, targetSize * 0.36f, targetSize * 0.16f, iconPaint)
                    val bodyRect = RectF(pad * 1.1f, targetSize * 0.58f, targetSize - (pad * 1.1f), targetSize - pad * 0.6f)
                    iconCanvas.drawRoundRect(bodyRect, targetSize * 0.2f, targetSize * 0.2f, iconPaint)
                }
                LogoBadge.FILE -> {
                    val docRect = RectF(pad * 1.2f, pad, targetSize - (pad * 1.2f), targetSize - pad)
                    iconCanvas.drawRoundRect(docRect, targetSize * 0.08f, targetSize * 0.08f, iconPaint)
                    val linePaint = Paint().apply {
                        color = customization.backgroundColor
                        strokeWidth = targetSize * 0.06f
                        strokeCap = Paint.Cap.ROUND
                    }
                    iconCanvas.drawLine(pad * 1.6f, targetSize * 0.4f, targetSize - (pad * 1.6f), targetSize * 0.4f, linePaint)
                    iconCanvas.drawLine(pad * 1.6f, targetSize * 0.55f, targetSize - (pad * 1.6f), targetSize * 0.55f, linePaint)
                    iconCanvas.drawLine(pad * 1.6f, targetSize * 0.7f, targetSize * 0.6f, targetSize * 0.7f, linePaint)
                }
                LogoBadge.LOCATION -> {
                    val p = android.graphics.Path().apply {
                        moveTo(targetSize / 2f, targetSize - pad)
                        quadTo(targetSize - pad, targetSize * 0.5f, targetSize - pad, targetSize * 0.4f)
                        arcTo(RectF(pad, pad, targetSize - pad, targetSize * 0.7f), 0f, -180f)
                        quadTo(pad, targetSize * 0.5f, targetSize / 2f, targetSize - pad)
                        close()
                    }
                    iconCanvas.drawPath(p, iconPaint)
                    val holePaint = Paint().apply {
                        color = customization.backgroundColor
                        isAntiAlias = true
                        style = Paint.Style.FILL
                    }
                    iconCanvas.drawCircle(targetSize / 2f, targetSize * 0.38f, targetSize * 0.12f, holePaint)
                }
                LogoBadge.STAR -> {
                    val p = android.graphics.Path()
                    val midX = targetSize / 2f
                    val midY = targetSize / 2f
                    val outerR = targetSize * 0.38f
                    val innerR = outerR * 0.45f
                    for (i in 0 until 10) {
                        val r = if (i % 2 == 0) outerR else innerR
                        val angle = Math.PI / 5 * i - Math.PI / 2
                        val x = (midX + r * Math.cos(angle)).toFloat()
                        val y = (midY + r * Math.sin(angle)).toFloat()
                        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
                    }
                    p.close()
                    iconCanvas.drawPath(p, iconPaint)
                }
                else -> {}
            }
            return iconBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = Math.min(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
        }
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawOval(rect, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return output
    }
}
