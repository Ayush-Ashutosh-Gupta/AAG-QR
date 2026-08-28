package com.example.qr.ui.components

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

val PRESET_FG_COLORS = listOf(
    0xFF0F172A.toInt() to "Navy Slate",
    0xFF000000.toInt() to "Pure Black",
    0xFF1E40AF.toInt() to "Royal Blue",
    0xFF047857.toInt() to "Emerald",
    0xFF6D28D9.toInt() to "Purple Violet",
    0xFFB91C1C.toInt() to "Crimson Red",
    0xFF0E7490.toInt() to "Cyan Teal",
    0xFFC2410C.toInt() to "Sunset Orange",
    0xFF4338CA.toInt() to "Indigo"
)

val PRESET_BG_COLORS = listOf(
    0xFFFFFFFF.toInt() to "Pure White",
    0xFFF8FAFC.toInt() to "Soft Slate",
    0xFFFEF3C7.toInt() to "Warm Amber",
    0xFFF0FDF4.toInt() to "Mint Tint",
    0xFFEFF6FF.toInt() to "Sky Tint",
    0xFFFAF5FF.toInt() to "Lavender",
    0xFF18181B.toInt() to "Dark Slate",
    0xFF000000.toInt() to "Pitch Dark"
)

@Composable
fun ColorPickerSection(
    fgColor: Int,
    bgColor: Int,
    isGradient: Boolean,
    gradientEndColor: Int,
    onFgColorChange: (Int) -> Unit,
    onBgColorChange: (Int) -> Unit,
    onGradientToggle: (Boolean) -> Unit,
    onGradientEndChange: (Int) -> Unit
) {
    var showCustomHex by remember { mutableStateOf(false) }
    var customHexText by remember { mutableStateOf(String.format("%06X", 0xFFFFFF and fgColor)) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        // Foreground Color Palette
        Text(
            text = "Foreground Color",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PRESET_FG_COLORS.forEach { (colorInt, name) ->
                val isSelected = fgColor == colorInt
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(colorInt))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable { onFgColorChange(colorInt) }
                        .testTag("fg_color_${name.replace(" ", "_")}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (colorInt == 0xFFFFFFFF.toInt()) Color.Black else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Background Color Palette
        Text(
            text = "Background Color",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PRESET_BG_COLORS.forEach { (colorInt, name) ->
                val isSelected = bgColor == colorInt
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(colorInt))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .clickable { onBgColorChange(colorInt) }
                        .testTag("bg_color_${name.replace(" ", "_")}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (colorInt == 0xFF000000.toInt() || colorInt == 0xFF18181B.toInt()) Color.White else Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Gradient & Custom Hex Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Dual-Color Gradient",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isGradient,
                    onCheckedChange = onGradientToggle,
                    modifier = Modifier.testTag("gradient_switch")
                )
            }

            FilterChip(
                selected = showCustomHex,
                onClick = { showCustomHex = !showCustomHex },
                label = { Text("Custom Hex") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.testTag("custom_hex_chip")
            )
        }

        AnimatedVisibility(visible = isGradient) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "Gradient Secondary Accent",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PRESET_FG_COLORS.forEach { (colorInt, name) ->
                        val isSelected = gradientEndColor == colorInt
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable { onGradientEndChange(colorInt) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = showCustomHex) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customHexText,
                    onValueChange = { input ->
                        val clean = input.filter { it.isLetterOrDigit() }.take(6)
                        customHexText = clean
                        if (clean.length == 6) {
                            try {
                                val parsed = android.graphics.Color.parseColor("#$clean")
                                onFgColorChange(parsed)
                            } catch (_: Exception) {}
                        }
                    },
                    label = { Text("Hex Color (#RRGGBB)") },
                    prefix = { Text("#") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("custom_hex_input")
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(fgColor))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                )
            }
        }
    }
}
