package com.example.qr.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShapeLine
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.qr.engine.CornerStyle
import com.example.qr.engine.DotStyle
import com.example.qr.engine.ErrorCorrection
import com.example.qr.engine.LogoBadge
import com.example.qr.engine.QrCustomization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCustomizationPanel(
    customization: QrCustomization,
    onCustomizationChange: (QrCustomization) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("customization_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Design & Customization",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${customization.dotStyle.displayName} • ${customization.cornerStyle.displayName} • ${customization.logoBadge.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.testTag("expand_customization_button")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {

                    // 1. Colors & Gradients
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

                    Spacer(modifier = Modifier.height(18.dp))

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
                                modifier = Modifier.testTag("dot_style_${style.name}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Corner / Finder Pattern Eye Style
                    Text(
                        text = "Corner Finder Eye Style",
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
                                        imageVector = getLogoBadgeIcon(badge),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.testTag("logo_badge_${badge.name}")
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
                }
            }
        }
    }
}

private fun getLogoBadgeIcon(badge: LogoBadge): ImageVector {
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
