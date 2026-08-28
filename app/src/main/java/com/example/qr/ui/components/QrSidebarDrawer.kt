package com.example.qr.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShapeLine
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.qr.engine.CornerStyle
import com.example.qr.engine.DotStyle
import com.example.qr.engine.ErrorCorrection
import com.example.qr.engine.LogoBadge
import com.example.qr.engine.QrCustomization
import com.example.qr.engine.QrPayloadType

import androidx.compose.foundation.layout.widthIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrSidebarDrawer(
    selectedType: QrPayloadType,
    onSelectType: (QrPayloadType) -> Unit,
    customization: QrCustomization,
    onCustomizationChange: (QrCustomization) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onCustomizationChange(
                customization.copy(
                    logoBadge = LogoBadge.CUSTOM,
                    customLogoUri = uri.toString(),
                    errorCorrection = ErrorCorrection.HIGH
                )
            )
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .fillMaxHeight()
            .testTag("qr_sidebar_drawer"),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sidebar Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Content Types & Style Lab",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier.testTag("close_sidebar_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Sidebar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 1: CONTENT TYPE SELECTOR
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "1. Select Content Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            // List of Content Types
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QrPayloadType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectType(type)
                            }
                            .testTag("sidebar_type_${type.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(type),
                                contentDescription = type.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = type.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = getTypeDescription(type),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2: DESIGN & CUSTOMIZATION
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "2. Style & Design Lab",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Color Customization
            ColorPickerSection(
                fgColor = customization.foregroundColor,
                bgColor = customization.backgroundColor,
                isGradient = customization.isGradient,
                gradientEndColor = customization.gradientColorEnd,
                onFgColorChange = { onCustomizationChange(customization.copy(foregroundColor = it)) },
                onBgColorChange = { onCustomizationChange(customization.copy(backgroundColor = it)) },
                onGradientToggle = { onCustomizationChange(customization.copy(isGradient = it)) },
                onGradientEndChange = { onCustomizationChange(customization.copy(gradientColorEnd = it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Matrix Dot Style
            Text(
                text = "Dot & Pattern Style",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DotStyle.values().forEach { style ->
                    FilterChip(
                        selected = customization.dotStyle == style,
                        onClick = { onCustomizationChange(customization.copy(dotStyle = style)) },
                        label = { Text(style.displayName) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (style) {
                                    DotStyle.SQUARE -> Icons.Default.CropSquare
                                    DotStyle.ROUNDED -> Icons.Default.ShapeLine
                                    DotStyle.CIRCLE -> Icons.Default.Circle
                                    DotStyle.SMOOTH -> Icons.Default.Style
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("sidebar_dot_style_${style.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Corner / Finder Pattern Eye Style
            Text(
                text = "Corner Eye Style",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                CornerStyle.values().forEachIndexed { index, corner ->
                    SegmentedButton(
                        selected = customization.cornerStyle == corner,
                        onClick = { onCustomizationChange(customization.copy(cornerStyle = corner)) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = CornerStyle.values().size)
                    ) {
                        Text(corner.displayName)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Center Logo / Branding Overlay
            Text(
                text = "Center Logo & Brand Overlay",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LogoBadge.values().forEach { badge ->
                    val isSelected = customization.logoBadge == badge
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (badge == LogoBadge.CUSTOM) {
                                photoPickerLauncher.launch("image/*")
                            } else {
                                onCustomizationChange(
                                    customization.copy(
                                        logoBadge = badge,
                                        errorCorrection = if (badge != LogoBadge.NONE) ErrorCorrection.HIGH else customization.errorCorrection
                                    )
                                )
                            }
                        },
                        label = { Text(badge.displayName) },
                        leadingIcon = {
                            Icon(
                                imageVector = getBadgeIcon(badge),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("sidebar_logo_badge_${badge.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Error Correction Level
            Text(
                text = "Error Correction Capacity",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ErrorCorrection.values().forEachIndexed { index, ec ->
                    SegmentedButton(
                        selected = customization.errorCorrection == ec,
                        onClick = { onCustomizationChange(customization.copy(errorCorrection = ec)) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ErrorCorrection.values().size)
                    ) {
                        Text(ec.level)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Close Action
            Button(
                onClick = onCloseDrawer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sidebar_apply_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Return to Generator", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun getCategoryIcon(type: QrPayloadType): ImageVector {
    return when (type) {
        QrPayloadType.URL -> Icons.Default.Language
        QrPayloadType.TEXT -> Icons.Default.TextFields
        QrPayloadType.DOCUMENT -> Icons.Default.Description
        QrPayloadType.WIFI -> Icons.Default.Wifi
        QrPayloadType.CONTACT -> Icons.Default.ContactPhone
        QrPayloadType.EMAIL -> Icons.Default.AlternateEmail
        QrPayloadType.PHONE -> Icons.Default.Phone
        QrPayloadType.SMS -> Icons.Default.Message
        QrPayloadType.LOCATION -> Icons.Default.LocationOn
        QrPayloadType.CALENDAR -> Icons.Default.CalendarMonth
    }
}

private fun getTypeDescription(type: QrPayloadType): String {
    return when (type) {
        QrPayloadType.URL -> "Website, portfolio, or web links"
        QrPayloadType.TEXT -> "Plain notes, codes, and messages"
        QrPayloadType.DOCUMENT -> "PDFs, images, and cloud files"
        QrPayloadType.WIFI -> "Instant auto-join Wi-Fi network"
        QrPayloadType.CONTACT -> "Digital business card (vCard)"
        QrPayloadType.EMAIL -> "Pre-filled email recipient & body"
        QrPayloadType.PHONE -> "Direct phone number dialer"
        QrPayloadType.SMS -> "Pre-filled text message"
        QrPayloadType.LOCATION -> "GPS coordinates & maps"
        QrPayloadType.CALENDAR -> "Events, date, and reminders"
    }
}

private fun getBadgeIcon(badge: LogoBadge): ImageVector {
    return when (badge) {
        LogoBadge.NONE -> Icons.Default.CropSquare
        LogoBadge.SHIELD -> Icons.Default.Security
        LogoBadge.WIFI -> Icons.Default.Wifi
        LogoBadge.LINK -> Icons.Default.Link
        LogoBadge.CONTACT -> Icons.Default.ContactPage
        LogoBadge.FILE -> Icons.Default.Description
        LogoBadge.LOCATION -> Icons.Default.LocationOn
        LogoBadge.STAR -> Icons.Default.Star
        LogoBadge.CUSTOM -> Icons.Default.AddPhotoAlternate
    }
}
