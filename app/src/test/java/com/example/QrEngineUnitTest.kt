package com.example

import com.example.qr.engine.CornerStyle
import com.example.qr.engine.DotStyle
import com.example.qr.engine.ErrorCorrection
import com.example.qr.engine.LogoBadge
import com.example.qr.engine.QrCodeGenerator
import com.example.qr.engine.QrCustomization
import com.example.qr.engine.QrPayloadBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QrEngineUnitTest {

    @Test
    fun testWifiPayloadFormat() {
        val wifiPayload = QrPayloadBuilder.buildWifi(
            ssid = "MyHomeNetwork",
            password = "SecretPassword123",
            authType = "WPA",
            isHidden = false
        )
        assertEquals("WIFI:T:WPA;S:MyHomeNetwork;P:SecretPassword123;H:false;;", wifiPayload)
    }

    @Test
    fun testVCardPayloadFormat() {
        val vcard = QrPayloadBuilder.buildVCard(
            firstName = "John",
            lastName = "Doe",
            phone = "+1234567890",
            email = "john@example.com",
            organization = "Acme Inc"
        )
        assertTrue(vcard.startsWith("BEGIN:VCARD"))
        assertTrue(vcard.contains("N:Doe;John;;;"))
        assertTrue(vcard.contains("FN:John Doe"))
        assertTrue(vcard.contains("TEL;TYPE=CELL:+1234567890"))
        assertTrue(vcard.endsWith("END:VCARD"))
    }

    @Test
    fun testUrlPrefixing() {
        val raw = "example.com"
        val formatted = QrPayloadBuilder.buildUrl(raw)
        assertEquals("https://example.com", formatted)
    }

    @Test
    fun testDynamicQrUrl() {
        val dynUrl = QrPayloadBuilder.buildDynamicQrUrl("promo2026")
        assertEquals("https://qr.local/dyn/promo2026", dynUrl)
    }

    @Test
    fun testDocumentSharePayload() {
        val docUrl = QrPayloadBuilder.buildDocumentShare(
            fileName = "Report.pdf",
            fileType = "PDF",
            fileSizeBytes = 1048576L,
            shareUrlOrContent = "https://drive.google.com/file/d/xyz"
        )
        assertEquals("https://drive.google.com/file/d/xyz", docUrl)

        val docRaw = QrPayloadBuilder.buildDocumentShare(
            fileName = "Notes.txt",
            fileType = "TEXT",
            fileSizeBytes = 512L,
            shareUrlOrContent = "Direct content note"
        )
        assertTrue(docRaw.startsWith("DOC:name=Notes.txt;type=TEXT"))
    }

    @Test
    fun testGeoLocationPayload() {
        val geo = QrPayloadBuilder.buildGeoLocation(
            latitude = 37.7749,
            longitude = -122.4194,
            label = "San Francisco"
        )
        assertTrue(geo.startsWith("geo:37.7749,-122.4194?q=37.7749,-122.4194(San%20Francisco)"))
    }

    @Test
    fun testQrBitmapGeneration() {
        val bitmap = QrCodeGenerator.generateQrBitmap(
            content = "https://example.com",
            size = 300,
            customization = QrCustomization(
                dotStyle = DotStyle.ROUNDED,
                cornerStyle = CornerStyle.ROUNDED,
                errorCorrection = ErrorCorrection.HIGH,
                logoBadge = LogoBadge.SHIELD
            )
        )
        assertNotNull(bitmap)
        assertEquals(300, bitmap?.width)
        assertEquals(300, bitmap?.height)
    }
}
