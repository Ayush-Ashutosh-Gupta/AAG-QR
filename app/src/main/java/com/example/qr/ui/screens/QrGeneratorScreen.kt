package com.example.qr.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qr.engine.QrPayloadType
import com.example.qr.ui.QrViewModel
import com.example.qr.ui.components.InteractiveMapPicker
import com.example.qr.ui.components.QrCustomizationPanel
import com.example.qr.ui.components.QrPreviewCard
import java.io.BufferedReader
import java.io.InputStreamReader

import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.qr.ui.components.QrSidebarDrawer

val SOCIAL_PRESETS = listOf(
    "GitHub" to "https://github.com/",
    "LinkedIn" to "https://linkedin.com/in/",
    "YouTube" to "https://youtube.com/@",
    "X / Twitter" to "https://x.com/",
    "Instagram" to "https://instagram.com/",
    "TikTok" to "https://tiktok.com/@",
    "WhatsApp" to "https://wa.me/",
    "Spotify" to "https://open.spotify.com/user/"
)

val CLOUD_DOC_PRESETS = listOf(
    "Google Drive" to "https://drive.google.com/file/d/",
    "Dropbox" to "https://www.dropbox.com/scl/fi/",
    "OneDrive" to "https://1drv.ms/",
    "iCloud" to "https://www.icloud.com/iclouddrive/",
    "Canva / PDF" to "https://www.canva.com/design/"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrGeneratorScreen(
    viewModel: QrViewModel,
    isWideScreen: Boolean = false,
    onOpenSidebar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val customization by viewModel.customization.collectAsStateWithLifecycle()
    val generatedBitmap by viewModel.generatedBitmap.collectAsStateWithLifecycle()
    val currentPayload by viewModel.currentPayload.collectAsStateWithLifecycle()
    val isQrStale by viewModel.isQrStale.collectAsStateWithLifecycle()

    var showWifiPassword by remember { mutableStateOf(false) }
    var docMode by remember { mutableStateOf(1) } // 0: Cloud / Direct Link, 1: Device File (24h Hosting)
    var showSidebarSheet by remember { mutableStateOf(false) }

    val openSidebar = {
        showSidebarSheet = true
        onOpenSidebar()
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.selectDeviceFile(uri)
            docMode = 1
            Toast.makeText(context, "File chosen. Tap 'Upload to Firebase & Generate QR' below!", Toast.LENGTH_LONG).show()
        }
    }

    if (isWideScreen) {
        // Landscape / Tablet Dual-Column Layout
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Left Column: Sticky QR Preview & Generation Trigger
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Generate QR Action Button
                Button(
                    onClick = { viewModel.generateQrNow(showToast = true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_qr_button_wide"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isQrStale) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (isQrStale) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isQrStale) 6.dp else 2.dp)
                ) {
                    Icon(
                        imageVector = if (isQrStale) Icons.Default.Refresh else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isQrStale) "✨ Generate QR Code" else "✓ QR Code Ready (Tap to Refresh)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                QrPreviewCard(
                    bitmap = generatedBitmap,
                    payloadType = formState.selectedType,
                    payloadText = currentPayload,
                    onSaveToGallery = { viewModel.saveQrToGallery("QR_${formState.selectedType.name}") },
                    onShare = { viewModel.shareCurrentQr("QR_${formState.selectedType.name}") },
                    onCopyPayload = { viewModel.copyCurrentPayload() },
                    onSaveToLibrary = { viewModel.saveCurrentQrToHistory() }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Right Column: Type Banner & Input Form
            Column(
                modifier = Modifier
                    .weight(1.25f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Sidebar Lab Quick Access Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openSidebar() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(formState.selectedType),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = formState.selectedType.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Styling & types configured via Sidebar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = openSidebar,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("open_sidebar_button_wide")
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Customize & Types", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input Form Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_form_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Quick 1-Tap Category Switcher Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QrPayloadType.entries.forEach { type ->
                                val isSelected = formState.selectedType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectPayloadType(type) },
                                    label = { Text(type.label, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = getCategoryIcon(type),
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        // Category Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(formState.selectedType),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${formState.selectedType.label} Data",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isQrStale) "⚠️ Changes pending — Tap Generate QR" else "QR Preview is up to date",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isQrStale) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic Category Fields
                        RenderCategoryForm(
                            formState = formState,
                            viewModel = viewModel,
                            showWifiPassword = showWifiPassword,
                            onToggleWifiPassword = { showWifiPassword = !showWifiPassword },
                            docMode = docMode,
                            onDocModeChange = { docMode = it },
                            onPickFile = { filePickerLauncher.launch("*/*") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    } else {
        // Portrait Layout: Streamlined, Lag-Free Single Column Flow
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Quick Type & Sidebar Header Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openSidebar() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(formState.selectedType),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = formState.selectedType.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap to open Customize & Style Lab",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = openSidebar,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("open_sidebar_button")
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Customize & Types", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Input Form Content Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_form_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f)
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Quick 1-Tap Category Switcher Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QrPayloadType.entries.forEach { type ->
                            val isSelected = formState.selectedType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectPayloadType(type) },
                                label = { Text(type.label, style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getCategoryIcon(type),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(formState.selectedType),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${formState.selectedType.label} Data",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isQrStale) "⚠️ Changes pending — Tap Generate QR" else "Preview is up to date",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isQrStale) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic Category Fields
                    RenderCategoryForm(
                        formState = formState,
                        viewModel = viewModel,
                        showWifiPassword = showWifiPassword,
                        onToggleWifiPassword = { showWifiPassword = !showWifiPassword },
                        docMode = docMode,
                        onDocModeChange = { docMode = it },
                        onPickFile = { filePickerLauncher.launch("*/*") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent "✨ Generate QR Code" Action Button
            Button(
                onClick = { viewModel.generateQrNow(showToast = true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_qr_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isQrStale) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isQrStale) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isQrStale) 6.dp else 2.dp)
            ) {
                Icon(
                    imageVector = if (isQrStale) Icons.Default.Refresh else Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isQrStale) "✨ Generate QR Code" else "✓ QR Code Ready (Tap to Refresh)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero QR Preview Card
            QrPreviewCard(
                bitmap = generatedBitmap,
                payloadType = formState.selectedType,
                payloadText = currentPayload,
                onSaveToGallery = { viewModel.saveQrToGallery("QR_${formState.selectedType.name}") },
                onShare = { viewModel.shareCurrentQr("QR_${formState.selectedType.name}") },
                onCopyPayload = { viewModel.copyCurrentPayload() },
                onSaveToLibrary = { viewModel.saveCurrentQrToHistory() }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showSidebarSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSidebarSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            QrSidebarDrawer(
                selectedType = formState.selectedType,
                onSelectType = { newType ->
                    viewModel.selectPayloadType(newType)
                    showSidebarSheet = false
                },
                customization = customization,
                onCustomizationChange = { viewModel.updateCustomizationDirect(it) },
                onCloseDrawer = { showSidebarSheet = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
            )
        }
    }
}

@Composable
private fun RenderCategoryForm(
    formState: com.example.qr.ui.QrGeneratorFormState,
    viewModel: QrViewModel,
    showWifiPassword: Boolean,
    onToggleWifiPassword: () -> Unit,
    docMode: Int,
    onDocModeChange: (Int) -> Unit,
    onPickFile: () -> Unit
) {
    val context = LocalContext.current
    when (formState.selectedType) {
                    QrPayloadType.TEXT -> {
                        OutlinedTextField(
                            value = formState.textContent,
                            onValueChange = { viewModel.updateForm { s -> s.copy(textContent = it) } },
                            label = { Text("Plain Text or Note") },
                            placeholder = { Text("Enter any message, secret key, or instructions") },
                            minLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("text_content_input")
                        )
                    }

                    QrPayloadType.URL -> {
                        OutlinedTextField(
                            value = formState.urlContent,
                            onValueChange = { viewModel.updateForm { s -> s.copy(urlContent = it) } },
                            label = { Text("Website or Profile Link") },
                            placeholder = { Text("https://example.com or profile link") },
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("url_content_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Quick Social / Web Presets:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SOCIAL_PRESETS.forEach { (label, prefix) ->
                                FilterChip(
                                    selected = formState.urlContent.startsWith(prefix),
                                    onClick = {
                                        viewModel.updateForm { s ->
                                            s.copy(
                                                urlContent = if (s.urlContent.startsWith(prefix)) s.urlContent else prefix
                                            )
                                        }
                                    },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    QrPayloadType.WIFI -> {
                        OutlinedTextField(
                            value = formState.wifiSsid,
                            onValueChange = { viewModel.updateForm { s -> s.copy(wifiSsid = it) } },
                            label = { Text("Network Name (SSID)") },
                            leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wifi_ssid_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = formState.wifiPassword,
                            onValueChange = { viewModel.updateForm { s -> s.copy(wifiPassword = it) } },
                            label = { Text("Wi-Fi Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = onToggleWifiPassword) {
                                    Icon(
                                        imageVector = if (showWifiPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (showWifiPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wifi_password_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Security Type:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("WPA", "WEP", "nopass").forEach { auth ->
                                FilterChip(
                                    selected = formState.wifiAuthType == auth,
                                    onClick = { viewModel.updateForm { s -> s.copy(wifiAuthType = auth) } },
                                    label = { Text(if (auth == "nopass") "No Password" else auth) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateForm { s -> s.copy(wifiIsHidden = !s.wifiIsHidden) } },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = formState.wifiIsHidden,
                                onCheckedChange = { isHidden -> viewModel.updateForm { s -> s.copy(wifiIsHidden = isHidden) } }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Hidden Network (SSID not broadcast)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    QrPayloadType.CONTACT -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formState.contactFirstName,
                                onValueChange = { viewModel.updateForm { s -> s.copy(contactFirstName = it) } },
                                label = { Text("First Name") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("contact_first_name")
                            )
                            OutlinedTextField(
                                value = formState.contactLastName,
                                onValueChange = { viewModel.updateForm { s -> s.copy(contactLastName = it) } },
                                label = { Text("Last Name") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("contact_last_name")
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = formState.contactPhone,
                            onValueChange = { viewModel.updateForm { s -> s.copy(contactPhone = it) } },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contact_phone")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = formState.contactEmail,
                            onValueChange = { viewModel.updateForm { s -> s.copy(contactEmail = it) } },
                            label = { Text("Email Address") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contact_email")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formState.contactCompany,
                                onValueChange = { viewModel.updateForm { s -> s.copy(contactCompany = it) } },
                                label = { Text("Organization") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formState.contactJobTitle,
                                onValueChange = { viewModel.updateForm { s -> s.copy(contactJobTitle = it) } },
                                label = { Text("Job Title") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = formState.contactCity,
                                onValueChange = { viewModel.updateForm { s -> s.copy(contactCity = it) } },
                                label = { Text("City") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = formState.contactCountry,
                                onValueChange = { viewModel.updateForm { s -> s.copy(contactCountry = it) } },
                                label = { Text("Country") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    QrPayloadType.EMAIL -> {
                        OutlinedTextField(
                            value = formState.emailTo,
                            onValueChange = { viewModel.updateForm { s -> s.copy(emailTo = it) } },
                            label = { Text("Recipient Email") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_to_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = formState.emailSubject,
                            onValueChange = { viewModel.updateForm { s -> s.copy(emailSubject = it) } },
                            label = { Text("Subject Line") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_subject_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = formState.emailBody,
                            onValueChange = { viewModel.updateForm { s -> s.copy(emailBody = it) } },
                            label = { Text("Email Message Body") },
                            minLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_body_input")
                        )
                    }

                    QrPayloadType.PHONE -> {
                        OutlinedTextField(
                            value = formState.phoneNumber,
                            onValueChange = { viewModel.updateForm { s -> s.copy(phoneNumber = it) } },
                            label = { Text("Phone Number to Dial") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input")
                        )
                    }

                    QrPayloadType.SMS -> {
                        OutlinedTextField(
                            value = formState.phoneNumber,
                            onValueChange = { viewModel.updateForm { s -> s.copy(phoneNumber = it) } },
                            label = { Text("Recipient Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sms_phone_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = formState.smsMessage,
                            onValueChange = { viewModel.updateForm { s -> s.copy(smsMessage = it) } },
                            label = { Text("SMS Message Body") },
                            minLines = 2,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sms_body_input")
                        )
                    }

                    QrPayloadType.DOCUMENT -> {
                        val cloudUploadState by viewModel.cloudUploadState.collectAsStateWithLifecycle()
                        val hostedFiles by viewModel.allHostedFiles.collectAsStateWithLifecycle()

                        // Document Mode Switcher: Firebase Cloud Host vs Web Doc Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = docMode == 1,
                                onClick = { onDocModeChange(1) },
                                label = { Text("📱 Device File (Firebase Cloud)") },
                                leadingIcon = { Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = docMode == 0,
                                onClick = { onDocModeChange(0) },
                                label = { Text("🌐 Web Doc / Direct Link") },
                                leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (docMode == 1) {
                            // DEVICE FILE FIREBASE CLOUD HOSTING MODE (SAFE & AD-FREE)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = when (formState.docFileType) {
                                                        "PDF" -> Icons.Default.Description
                                                        "IMAGE" -> Icons.Default.Language
                                                        "VIDEO" -> Icons.Default.DynamicFeed
                                                        else -> Icons.Default.FolderOpen
                                                    },
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = formState.docFileName.ifEmpty { "No file chosen" },
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${formState.docFileType} • ${formatDocFileSize(formState.docFileSize)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedButton(
                                        onClick = onPickFile,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .testTag("pick_file_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Choose File from Phone Storage")
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Upload & Status Action
                                    when (cloudUploadState) {
                                        is com.example.qr.engine.CloudUploadState.Uploading -> {
                                            val uploadProgress = (cloudUploadState as com.example.qr.engine.CloudUploadState.Uploading).progressPercent
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "Uploading safely to Firebase Cloud...",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = "$uploadProgress%",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                LinearProgressIndicator(
                                                    progress = uploadProgress / 100f,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                )
                                            }
                                        }

                                        else -> {
                                            if (formState.isDocHostedOnline) {
                                                // Live Online QR is Active
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.CloudDone,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = "✓ Live Direct Link Active",
                                                                style = MaterialTheme.typography.labelMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                            Spacer(modifier = Modifier.weight(1f))
                                                            Surface(
                                                                shape = RoundedCornerShape(100.dp),
                                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                            ) {
                                                                Row(
                                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Lock,
                                                                        contentDescription = null,
                                                                        modifier = Modifier.size(12.dp),
                                                                        tint = MaterialTheme.colorScheme.primary
                                                                    )
                                                                    Spacer(modifier = Modifier.width(4.dp))
                                                                    Text(
                                                                        text = "Direct Download",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = MaterialTheme.colorScheme.primary
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(
                                                            text = formState.docShareUrl,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            maxLines = 1,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )

                                                        Spacer(modifier = Modifier.height(10.dp))
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            OutlinedButton(
                                                                onClick = {
                                                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Hosted Link", formState.docShareUrl))
                                                                    Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                                                                },
                                                                modifier = Modifier.weight(1f),
                                                                shape = RoundedCornerShape(10.dp)
                                                            ) {
                                                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("Copy Link", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                            Button(
                                                                onClick = {
                                                                    try {
                                                                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(formState.docShareUrl))
                                                                        context.startActivity(browserIntent)
                                                                    } catch (_: Exception) {
                                                                        Toast.makeText(context, "Cannot open browser", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                },
                                                                modifier = Modifier.weight(1f),
                                                                shape = RoundedCornerShape(10.dp)
                                                            ) {
                                                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("Open Link", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                // Not yet hosted - Show upload CTA
                                                Button(
                                                    onClick = { viewModel.uploadSelectedFileToCloud() },
                                                    enabled = formState.docSelectedUri != null,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(48.dp)
                                                        .testTag("upload_file_cloud_button"),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Upload & Generate QR Code", fontWeight = FontWeight.Bold)
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "🔒 Direct hosting via Catbox / Litterbox. Generates direct, safe download links without landing pages, ads, or APK prompts.",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Active Hosted Files in Storage
                            if (hostedFiles.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Your Uploaded Cloud Files (${hostedFiles.size}):",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    hostedFiles.forEach { file ->
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = file.fileName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1
                                                    )
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = formatDocFileSize(file.fileSizeBytes),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "• ${formatDocRemainingTime(file.expiresAt)}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                IconButton(
                                                    onClick = { viewModel.loadHostedFileIntoGenerator(file) },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Refresh,
                                                        contentDescription = "Load into QR",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { viewModel.deleteHostedFile(file) },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete from Cloud",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // MANUAL / WEB CLOUD LINK MODE
                            OutlinedTextField(
                                value = formState.docFileName,
                                onValueChange = { viewModel.updateForm { s -> s.copy(docFileName = it) } },
                                label = { Text("Document / File Name") },
                                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("doc_name_input")
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = formState.docShareUrl,
                                onValueChange = { viewModel.updateForm { s -> s.copy(docShareUrl = it) } },
                                label = { Text("Direct View / Download Link") },
                                placeholder = { Text("https://drive.google.com/file/d/... or direct doc link") },
                                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("doc_url_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Cloud Storage Presets:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CLOUD_DOC_PRESETS.forEach { (name, prefix) ->
                                    FilterChip(
                                        selected = formState.docShareUrl.startsWith(prefix),
                                        onClick = {
                                            viewModel.updateForm { s ->
                                                s.copy(docShareUrl = if (s.docShareUrl.startsWith(prefix)) s.docShareUrl else prefix)
                                            }
                                        },
                                        label = { Text(name) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Document Category:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val docTypes = listOf("PDF", "IMAGE", "VIDEO", "DOCX", "PPTX", "XLSX")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                docTypes.forEach { dt ->
                                    FilterChip(
                                        selected = formState.docFileType == dt,
                                        onClick = { viewModel.updateForm { s -> s.copy(docFileType = dt) } },
                                        label = { Text(dt) }
                                    )
                                }
                            }
                        }
                    }

                    QrPayloadType.CALENDAR -> {
                        OutlinedTextField(
                            value = formState.calTitle,
                            onValueChange = { viewModel.updateForm { s -> s.copy(calTitle = it) } },
                            label = { Text("Event Title") },
                            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cal_title_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = formState.calLocation,
                            onValueChange = { viewModel.updateForm { s -> s.copy(calLocation = it) } },
                            label = { Text("Event Location") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = formState.calDescription,
                            onValueChange = { viewModel.updateForm { s -> s.copy(calDescription = it) } },
                            label = { Text("Event Description") },
                            minLines = 2,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    QrPayloadType.LOCATION -> {
                        // Interactive Map & Current GPS location picker
                        InteractiveMapPicker(
                            latitude = formState.locLatitude,
                            longitude = formState.locLongitude,
                            label = formState.locLabel,
                            onLocationChange = { lat, lng, newLabel ->
                                viewModel.updateForm { s ->
                                    s.copy(
                                        locLatitude = lat,
                                        locLongitude = lng,
                                        locLabel = newLabel
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = formState.locLabel,
                            onValueChange = { viewModel.updateForm { s -> s.copy(locLabel = it) } },
                            label = { Text("Location Name / Landmark") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("location_label_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = formState.locLatitude.toString(),
                                onValueChange = {
                                    it.toDoubleOrNull()?.let { lat -> viewModel.updateForm { s -> s.copy(locLatitude = lat) } }
                                },
                                label = { Text("Latitude") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("loc_lat_input")
                            )
                            OutlinedTextField(
                                value = formState.locLongitude.toString(),
                                onValueChange = {
                                    it.toDoubleOrNull()?.let { lng -> viewModel.updateForm { s -> s.copy(locLongitude = lng) } }
                                },
                                label = { Text("Longitude") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("loc_lng_input")
                            )
                        }
                    }
                }
}

private fun getCategoryIcon(type: QrPayloadType): ImageVector {
    return when (type) {
        QrPayloadType.TEXT -> Icons.Default.TextFields
        QrPayloadType.URL -> Icons.Default.Language
        QrPayloadType.WIFI -> Icons.Default.Wifi
        QrPayloadType.CONTACT -> Icons.Default.ContactPhone
        QrPayloadType.EMAIL -> Icons.Default.AlternateEmail
        QrPayloadType.PHONE -> Icons.Default.Phone
        QrPayloadType.SMS -> Icons.Default.Message
        QrPayloadType.DOCUMENT -> Icons.Default.Description
        QrPayloadType.CALENDAR -> Icons.Default.CalendarMonth
        QrPayloadType.LOCATION -> Icons.Default.LocationOn
    }
}

private fun formatDocFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val safeGroup = digitGroups.coerceIn(0, units.size - 1)
    val count = bytes / Math.pow(1024.0, safeGroup.toDouble())
    return String.format(Locale.getDefault(), "%.1f %s", count, units[safeGroup])
}

private fun formatDocRemainingTime(expiresAt: Long?): String {
    if (expiresAt == null) return "24h left"
    val diff = expiresAt - System.currentTimeMillis()
    if (diff <= 0) return "Expired"
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
    return if (hours > 0) "${hours}h ${minutes}m left" else "${minutes}m left"
}

