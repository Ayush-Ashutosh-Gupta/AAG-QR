package com.example.qr.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Locale

data class CityPreset(
    val name: String,
    val country: String,
    val lat: Double,
    val lng: Double
)

val POPULAR_LOCATIONS = listOf(
    CityPreset("San Francisco", "USA", 37.7749, -122.4194),
    CityPreset("New York", "USA", 40.7128, -74.0060),
    CityPreset("London", "UK", 51.5074, -0.1278),
    CityPreset("Paris", "France", 48.8566, 2.3522),
    CityPreset("Tokyo", "Japan", 35.6762, 139.6503),
    CityPreset("Dubai", "UAE", 25.2048, 55.2708),
    CityPreset("Singapore", "Singapore", 1.3521, 103.8198),
    CityPreset("Sydney", "Australia", -33.8688, 151.2093),
    CityPreset("Berlin", "Germany", 52.5200, 13.4050),
    CityPreset("Rome", "Italy", 41.9028, 12.4964),
    CityPreset("Cairo", "Egypt", 30.0444, 31.2357),
    CityPreset("Rio de Janeiro", "Brazil", -22.9068, -43.1729)
)

@Composable
fun InteractiveMapPicker(
    latitude: Double,
    longitude: Double,
    label: String,
    onLocationChange: (lat: Double, lng: Double, newLabel: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLocating by remember { mutableStateOf(false) }

    fun fetchCurrentDeviceLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
            return
        }

        isLocating = true
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            isLocating = false
            Toast.makeText(context, "Location service unavailable", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            var bestLoc: Location? = null
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                bestLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (bestLoc == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                bestLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            if (bestLoc == null && locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                bestLoc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            }

            if (bestLoc != null) {
                isLocating = false
                val formattedLat = String.format(Locale.US, "%.5f", bestLoc.latitude).toDoubleOrNull() ?: bestLoc.latitude
                val formattedLng = String.format(Locale.US, "%.5f", bestLoc.longitude).toDoubleOrNull() ?: bestLoc.longitude
                onLocationChange(formattedLat, formattedLng, "My Current Location")
                Toast.makeText(context, "Current location updated!", Toast.LENGTH_SHORT).show()
            } else {
                // Request a single fresh update
                val listener = object : LocationListener {
                    override fun onLocationChanged(loc: Location) {
                        isLocating = false
                        val formattedLat = String.format(Locale.US, "%.5f", loc.latitude).toDoubleOrNull() ?: loc.latitude
                        val formattedLng = String.format(Locale.US, "%.5f", loc.longitude).toDoubleOrNull() ?: loc.longitude
                        onLocationChange(formattedLat, formattedLng, "My Current Location")
                        Toast.makeText(context, "Location pinpointed!", Toast.LENGTH_SHORT).show()
                        locationManager.removeUpdates(this)
                    }
                    override fun onProviderDisabled(provider: String) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                }

                val provider = when {
                    locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                    else -> LocationManager.PASSIVE_PROVIDER
                }
                locationManager.requestSingleUpdate(provider, listener, null)
            }
        } catch (e: SecurityException) {
            isLocating = false
            Toast.makeText(context, "Permission error: ${e.message}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            isLocating = false
            Toast.makeText(context, "Unable to get GPS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchCurrentDeviceLocation()
        } else {
            Toast.makeText(context, "Location permission was denied", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_map_card"),
        shape = RoundedCornerShape(20.dp),
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
            // Header & GPS Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Interactive Map & GPS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap or drag crosshair on map to select coordinates",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Current Location (GPS) & Open Maps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val fine = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        val coarse = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (fine || coarse) {
                            fetchCurrentDeviceLocation()
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("get_current_location_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLocating) "Locating..." else "Use My Location",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = {
                        try {
                            val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        } catch (_: Exception) {
                            val webUri = Uri.parse("https://maps.google.com/?q=$latitude,$longitude")
                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("preview_in_maps_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test in Maps")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Glassmorphic Map Canvas with Crosshair Pin
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        RoundedCornerShape(16.dp)
                    )
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val lat = 90.0 - (offset.y / size.height) * 180.0
                            val lng = (offset.x / size.width) * 360.0 - 180.0
                            val roundedLat = String.format(Locale.US, "%.4f", lat.coerceIn(-85.0, 85.0)).toDouble()
                            val roundedLng = String.format(Locale.US, "%.4f", lng.coerceIn(-180.0, 180.0)).toDouble()
                            onLocationChange(roundedLat, roundedLng, "Pinned Location")
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val lat = 90.0 - (change.position.y / size.height) * 180.0
                            val lng = (change.position.x / size.width) * 360.0 - 180.0
                            val roundedLat = String.format(Locale.US, "%.4f", lat.coerceIn(-85.0, 85.0)).toDouble()
                            val roundedLng = String.format(Locale.US, "%.4f", lng.coerceIn(-180.0, 180.0)).toDouble()
                            onLocationChange(roundedLat, roundedLng, "Pinned Location")
                        }
                    }
                    .testTag("map_canvas")
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                val landColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)

                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw World Map Grid Lines (Equator, Prime Meridian, Lat/Long intervals)
                    // Latitude lines
                    for (i in 1..5) {
                        val y = (h / 6) * i
                        drawLine(
                            color = if (i == 3) gridColor.copy(alpha = 0.7f) else gridColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = if (i == 3) 2f else 1f
                        )
                    }
                    // Longitude lines
                    for (i in 1..7) {
                        val x = (w / 8) * i
                        drawLine(
                            color = if (i == 4) gridColor.copy(alpha = 0.7f) else gridColor,
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = if (i == 4) 2f else 1f
                        )
                    }

                    // Stylized Continent Silhouettes (Geometric representation)
                    // North America
                    val na = Path().apply {
                        moveTo(w * 0.12f, h * 0.22f)
                        lineTo(w * 0.28f, h * 0.20f)
                        lineTo(w * 0.32f, h * 0.40f)
                        lineTo(w * 0.20f, h * 0.46f)
                        close()
                    }
                    drawPath(na, landColor)

                    // South America
                    val sa = Path().apply {
                        moveTo(w * 0.24f, h * 0.50f)
                        lineTo(w * 0.33f, h * 0.52f)
                        lineTo(w * 0.28f, h * 0.78f)
                        lineTo(w * 0.22f, h * 0.62f)
                        close()
                    }
                    drawPath(sa, landColor)

                    // Europe & Asia
                    val eurasia = Path().apply {
                        moveTo(w * 0.44f, h * 0.18f)
                        lineTo(w * 0.85f, h * 0.22f)
                        lineTo(w * 0.80f, h * 0.48f)
                        lineTo(w * 0.55f, h * 0.46f)
                        lineTo(w * 0.42f, h * 0.35f)
                        close()
                    }
                    drawPath(eurasia, landColor)

                    // Africa
                    val africa = Path().apply {
                        moveTo(w * 0.45f, h * 0.40f)
                        lineTo(w * 0.58f, h * 0.42f)
                        lineTo(w * 0.54f, h * 0.72f)
                        lineTo(w * 0.45f, h * 0.58f)
                        close()
                    }
                    drawPath(africa, landColor)

                    // Australia
                    val australia = Path().apply {
                        moveTo(w * 0.75f, h * 0.62f)
                        lineTo(w * 0.86f, h * 0.64f)
                        lineTo(w * 0.84f, h * 0.78f)
                        lineTo(w * 0.73f, h * 0.74f)
                        close()
                    }
                    drawPath(australia, landColor)

                    // Map current lat/lng to canvas coordinates
                    val targetX = (((longitude + 180.0) / 360.0) * w).toFloat().coerceIn(12f, w - 12f)
                    val targetY = (((90.0 - latitude) / 180.0) * h).toFloat().coerceIn(12f, h - 12f)

                    // Pulsing Outer Radar Ring
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.25f),
                        radius = 28f,
                        center = Offset(targetX, targetY)
                    )
                    drawCircle(
                        color = secondaryColor.copy(alpha = 0.5f),
                        radius = 16f,
                        center = Offset(targetX, targetY),
                        style = Stroke(width = 2f)
                    )

                    // Target Crosshair
                    drawLine(
                        color = primaryColor,
                        start = Offset(targetX - 22f, targetY),
                        end = Offset(targetX + 22f, targetY),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = primaryColor,
                        start = Offset(targetX, targetY - 22f),
                        end = Offset(targetX, targetY + 22f),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )

                    // Center Solid Dot
                    drawCircle(
                        color = primaryColor,
                        radius = 6f,
                        center = Offset(targetX, targetY)
                    )
                }

                // Live Coordinate Overlay Tag on Canvas
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Navigation,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.4f°, %.4f°", latitude, longitude),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick City Presets
            Text(
                text = "Popular Locations:",
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
                POPULAR_LOCATIONS.forEach { city ->
                    val isSelected = label == city.name || (Math.abs(latitude - city.lat) < 0.01 && Math.abs(longitude - city.lng) < 0.01)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onLocationChange(city.lat, city.lng, "${city.name}, ${city.country}")
                        },
                        label = { Text("${city.name}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}
