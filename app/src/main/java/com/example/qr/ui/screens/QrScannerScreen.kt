package com.example.qr.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.qr.engine.QrMediaHelper
import com.example.qr.ui.QrViewModel
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun QrScannerScreen(
    viewModel: QrViewModel,
    isWideScreen: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var decodedResult by remember { mutableStateOf<String?>(null) }
    var detectedFormat by remember { mutableStateOf("QR Code") }
    var scanStatus by remember { mutableStateOf("Ready to scan offline") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scanStatus = "Decoding image offline..."
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }

                val decoded = decodeQrFromBitmap(bitmap)
                if (decoded != null) {
                    decodedResult = decoded
                    detectedFormat = detectQrType(decoded)
                    scanStatus = "Successfully decoded!"
                } else {
                    scanStatus = "No valid QR code detected in image."
                    Toast.makeText(context, "Could not find a QR Code in this image", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                scanStatus = "Error decoding image: ${e.message}"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Scanner Glass Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("scanner_hero_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Offline Barcode & QR Decoder",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Decodes all QR formats (WiFi, Docs, Location, Contacts, Links) directly on your device with 100% privacy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pick_image_scan_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scan QR from Image / Photo",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = scanStatus,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Decoded Result Card
        AnimatedVisibility(visible = decodedResult != null) {
            val result = decodedResult ?: ""

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("decoded_result_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = detectedFormat,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Offline Decoded",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Smart Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                QrMediaHelper.copyToClipboard(context, result)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy")
                        }

                        // Document Payload Handler (DOC: or document URLs)
                        if (result.startsWith("DOC:") || isDocumentUrl(result)) {
                            val targetUrl = extractDocumentUrl(result)
                            Button(
                                onClick = {
                                    try {
                                        val uri = Uri.parse(targetUrl)
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot open: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Doc")
                            }
                        } else if (result.startsWith("http://") || result.startsWith("https://")) {
                            Button(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result)))
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Link")
                            }
                        } else if (result.startsWith("WIFI:")) {
                            Button(
                                onClick = {
                                    val ssid = extractWifiParam(result, "S")
                                    val pass = extractWifiParam(result, "P")
                                    QrMediaHelper.copyToClipboard(context, pass, "Wi-Fi Password for $ssid")
                                    Toast.makeText(context, "Password copied for network '$ssid'", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Wi-Fi")
                            }
                        } else if (result.startsWith("BEGIN:VCARD")) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI).apply {
                                        putExtra(ContactsContract.Intents.Insert.NAME, extractVcardParam(result, "FN"))
                                        putExtra(ContactsContract.Intents.Insert.PHONE, extractVcardParam(result, "TEL"))
                                        putExtra(ContactsContract.Intents.Insert.EMAIL, extractVcardParam(result, "EMAIL"))
                                        putExtra(ContactsContract.Intents.Insert.COMPANY, extractVcardParam(result, "ORG"))
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        QrMediaHelper.copyToClipboard(context, result)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Contact")
                            }
                        } else if (result.startsWith("geo:") || result.contains("maps.google.com")) {
                            Button(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result)))
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Map")
                            }
                        } else if (result.startsWith("tel:")) {
                            Button(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(result)))
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call")
                            }
                        } else if (result.startsWith("smsto:")) {
                            Button(
                                onClick = {
                                    try {
                                        val parts = result.removePrefix("smsto:").split(":")
                                        val phone = parts.getOrNull(0) ?: ""
                                        val msg = parts.drop(1).joinToString(":")
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")).apply {
                                            putExtra("sms_body", msg)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send SMS")
                            }
                        } else if (result.startsWith("mailto:")) {
                            Button(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result)))
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AlternateEmail, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send Email")
                            }
                        } else if (result.startsWith("BEGIN:VEVENT")) {
                            Button(
                                onClick = {
                                    try {
                                        val title = extractVeventParam(result, "SUMMARY")
                                        val desc = extractVeventParam(result, "DESCRIPTION")
                                        val loc = extractVeventParam(result, "LOCATION")
                                        val intent = Intent(Intent.ACTION_INSERT).apply {
                                            data = CalendarContract.Events.CONTENT_URI
                                            putExtra(CalendarContract.Events.TITLE, title)
                                            putExtra(CalendarContract.Events.DESCRIPTION, desc)
                                            putExtra(CalendarContract.Events.EVENT_LOCATION, loc)
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Event")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun decodeQrFromBitmap(bitmap: Bitmap): String? {
    return try {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader()
        val result = reader.decode(binaryBitmap)
        result.text
    } catch (_: Exception) {
        null
    }
}

private fun detectQrType(content: String): String {
    return when {
        content.startsWith("DOC:") || isDocumentUrl(content) -> "Document / File"
        content.startsWith("WIFI:") -> "Wi-Fi Network"
        content.startsWith("BEGIN:VCARD") -> "Contact Card (vCard)"
        content.startsWith("BEGIN:VEVENT") -> "Calendar Event"
        content.startsWith("mailto:") -> "Email Message"
        content.startsWith("tel:") -> "Phone Number"
        content.startsWith("smsto:") -> "SMS Text"
        content.startsWith("geo:") || content.contains("maps.google.com") -> "Location & Map"
        content.startsWith("http://") || content.startsWith("https://") -> "Web / Social URL"
        else -> "Plain Text Note"
    }
}

private fun isDocumentUrl(text: String): Boolean {
    val lower = text.lowercase()
    return lower.contains("drive.google.com") || lower.contains("dropbox.com") ||
            lower.contains("1drv.ms") || lower.contains("onedrive") ||
            lower.contains("canva.com") || lower.endsWith(".pdf") ||
            lower.endsWith(".docx") || lower.endsWith(".doc") ||
            lower.endsWith(".xlsx") || lower.endsWith(".pptx") ||
            lower.endsWith(".png") || lower.endsWith(".jpg") ||
            lower.endsWith(".mp4")
}

private fun extractDocumentUrl(payload: String): String {
    if (payload.startsWith("http://") || payload.startsWith("https://")) {
        return payload
    }
    if (payload.startsWith("DOC:")) {
        val parts = payload.removePrefix("DOC:").split(";")
        for (part in parts) {
            if (part.startsWith("url=")) {
                val encodedUrl = part.removePrefix("url=")
                return try {
                    URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
                } catch (_: Exception) {
                    encodedUrl
                }
            }
        }
    }
    return payload
}

private fun extractWifiParam(payload: String, param: String): String {
    val regex = Regex("""$param:([^;]*);""")
    val match = regex.find(payload)
    return match?.groupValues?.get(1)?.replace("\\;", ";")?.replace("\\:", ":") ?: ""
}

private fun extractVcardParam(vcard: String, field: String): String {
    val line = vcard.lines().firstOrNull { it.startsWith(field) } ?: return ""
    return line.substringAfter(":", "").trim()
}

private fun extractVeventParam(vevent: String, field: String): String {
    val line = vevent.lines().firstOrNull { it.startsWith(field) } ?: return ""
    return line.substringAfter(":", "").trim()
}
