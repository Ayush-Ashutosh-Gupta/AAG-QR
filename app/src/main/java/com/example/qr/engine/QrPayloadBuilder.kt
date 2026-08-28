package com.example.qr.engine

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object QrPayloadBuilder {

    fun buildText(text: String): String = text.trim()

    fun buildUrl(rawUrl: String): String {
        var url = rawUrl.trim()
        if (url.isEmpty()) return ""
        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.contains("://")) {
            url = "https://$url"
        }
        return url
    }

    fun buildWifi(
        ssid: String,
        password: String,
        authType: String = "WPA", // WPA, WEP, nopass
        isHidden: Boolean = false
    ): String {
        val cleanSsid = escapeWifiString(ssid.trim())
        val cleanPass = escapeWifiString(password)
        val auth = when (authType.uppercase()) {
            "WPA", "WPA2", "WPA3", "WPA/WPA2" -> "WPA"
            "WEP" -> "WEP"
            else -> "nopass"
        }
        return "WIFI:T:$auth;S:$cleanSsid;P:$cleanPass;H:$isHidden;;"
    }

    private fun escapeWifiString(input: String): String {
        return input.replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace(":", "\\:")
            .replace("\"", "\\\"")
    }

    fun buildVCard(
        firstName: String,
        lastName: String,
        phone: String,
        email: String = "",
        organization: String = "",
        jobTitle: String = "",
        website: String = "",
        street: String = "",
        city: String = "",
        state: String = "",
        postalCode: String = "",
        country: String = "",
        notes: String = ""
    ): String {
        val sb = StringBuilder()
        sb.append("BEGIN:VCARD\n")
        sb.append("VERSION:3.0\n")
        val fn = listOf(firstName.trim(), lastName.trim()).filter { it.isNotEmpty() }.joinToString(" ")
        sb.append("N:${lastName.trim()};${firstName.trim()};;;\n")
        sb.append("FN:$fn\n")
        if (organization.isNotBlank()) sb.append("ORG:${organization.trim()}\n")
        if (jobTitle.isNotBlank()) sb.append("TITLE:${jobTitle.trim()}\n")
        if (phone.isNotBlank()) sb.append("TEL;TYPE=CELL:${phone.trim()}\n")
        if (email.isNotBlank()) sb.append("EMAIL;TYPE=INTERNET:${email.trim()}\n")
        if (website.isNotBlank()) sb.append("URL:${buildUrl(website)}\n")
        if (street.isNotBlank() || city.isNotBlank() || state.isNotBlank() || postalCode.isNotBlank() || country.isNotBlank()) {
            sb.append("ADR;TYPE=WORK:;;${street.trim()};${city.trim()};${state.trim()};${postalCode.trim()};${country.trim()}\n")
        }
        if (notes.isNotBlank()) sb.append("NOTE:${notes.trim()}\n")
        sb.append("END:VCARD")
        return sb.toString()
    }

    fun buildEmail(
        to: String,
        subject: String = "",
        body: String = ""
    ): String {
        val cleanTo = to.trim()
        if (subject.isEmpty() && body.isEmpty()) {
            return "mailto:$cleanTo"
        }
        val encodedSubject = urlEncode(subject)
        val encodedBody = urlEncode(body)
        val params = mutableListOf<String>()
        if (subject.isNotEmpty()) params.add("subject=$encodedSubject")
        if (body.isNotEmpty()) params.add("body=$encodedBody")
        return "mailto:$cleanTo?${params.joinToString("&")}"
    }

    fun buildPhone(phoneNumber: String): String {
        return "tel:${phoneNumber.trim()}"
    }

    fun buildSms(phoneNumber: String, message: String = ""): String {
        val phone = phoneNumber.trim()
        return if (message.isBlank()) {
            "smsto:$phone"
        } else {
            "smsto:$phone:$message"
        }
    }

    fun buildCalendarEvent(
        title: String,
        description: String,
        location: String,
        startDateTimeUtc: String,
        endDateTimeUtc: String
    ): String {
        val sb = StringBuilder()
        sb.append("BEGIN:VEVENT\n")
        sb.append("SUMMARY:${title.trim()}\n")
        if (description.isNotBlank()) sb.append("DESCRIPTION:${description.trim()}\n")
        if (location.isNotBlank()) sb.append("LOCATION:${location.trim()}\n")
        sb.append("DTSTART:$startDateTimeUtc\n")
        sb.append("DTEND:$endDateTimeUtc\n")
        sb.append("END:VEVENT")
        return sb.toString()
    }

    fun buildGeoLocation(
        latitude: Double,
        longitude: Double,
        label: String = ""
    ): String {
        return if (label.isNotBlank()) {
            val encodedLabel = urlEncode(label)
            "geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)"
        } else {
            "geo:$latitude,$longitude?q=$latitude,$longitude"
        }
    }

    /**
     * Builds Document QR payload:
     * Generates a direct clickable HTTPS document URL for phone cameras and scanners.
     */
    fun buildDocumentShare(
        fileName: String,
        fileType: String,
        fileSizeBytes: Long,
        shareUrlOrContent: String,
        notes: String = ""
    ): String {
        val trimmed = shareUrlOrContent.trim()
        if (trimmed.isNotBlank()) {
            return buildUrl(trimmed)
        }
        return ""
    }

    private fun urlEncode(str: String): String {
        return try {
            URLEncoder.encode(str, StandardCharsets.UTF_8.toString()).replace("+", "%20")
        } catch (_: Exception) {
            str
        }
    }
}
