package com.example.qr.engine

enum class QrPayloadType(val label: String, val category: String) {
    TEXT("Text", "General"),
    URL("Website / Link", "Web"),
    WIFI("Wi-Fi Network", "Connectivity"),
    CONTACT("Contact (vCard)", "Personal"),
    EMAIL("Email", "Communication"),
    PHONE("Phone Call", "Communication"),
    SMS("SMS Message", "Communication"),
    DOCUMENT("Document / File", "Files"),
    CALENDAR("Calendar Event", "Productivity"),
    LOCATION("Location / Map", "Navigation")
}

enum class DotStyle(val displayName: String) {
    SQUARE("Sharp Square"),
    ROUNDED("Rounded"),
    CIRCLE("Dots / Circles"),
    SMOOTH("Squircle")
}

enum class CornerStyle(val displayName: String) {
    SQUARE("Square"),
    ROUNDED("Rounded"),
    CIRCLE("Circular")
}

enum class ErrorCorrection(val displayName: String, val level: String) {
    LOW("Low (7%)", "L"),
    MEDIUM("Medium (15%)", "M"),
    QUARTILE("High (25%)", "Q"),
    HIGH("Maximum (30% - Best for Logos)", "H")
}

enum class LogoBadge(val displayName: String) {
    NONE("None"),
    SHIELD("Security"),
    WIFI("Wi-Fi"),
    LINK("Link"),
    CONTACT("Contact"),
    FILE("Document"),
    LOCATION("Location"),
    STAR("Star"),
    CUSTOM("Custom Photo")
}

data class QrCustomization(
    val foregroundColor: Int = 0xFF0F172A.toInt(), // Dark Navy/Slate
    val backgroundColor: Int = 0xFFFFFFFF.toInt(), // Pure White
    val isGradient: Boolean = false,
    val gradientColorEnd: Int = 0xFF2563EB.toInt(), // Royal Blue
    val dotStyle: DotStyle = DotStyle.SQUARE,
    val cornerStyle: CornerStyle = CornerStyle.ROUNDED,
    val errorCorrection: ErrorCorrection = ErrorCorrection.HIGH,
    val logoBadge: LogoBadge = LogoBadge.NONE,
    val customLogoUri: String? = null,
    val customLogoBase64: String? = null
)
