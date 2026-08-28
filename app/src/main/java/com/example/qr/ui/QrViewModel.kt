package com.example.qr.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.qr.data.AppDatabase
import com.example.qr.data.QrEntity
import com.example.qr.data.QrRepository
import com.example.qr.data.QrScanLogEntity
import com.example.qr.engine.CornerStyle
import com.example.qr.engine.DotStyle
import com.example.qr.engine.ErrorCorrection
import com.example.qr.engine.LogoBadge
import com.example.qr.engine.QrCodeGenerator
import com.example.qr.engine.QrCustomization
import com.example.qr.engine.QrMediaHelper
import com.example.qr.engine.QrPayloadBuilder
import com.example.qr.engine.QrPayloadType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class QrGeneratorFormState(
    val selectedType: QrPayloadType = QrPayloadType.URL,
    
    // Text
    val textContent: String = "Welcome to Private QR Generator! 100% offline & secure.",
    
    // URL
    val urlContent: String = "https://github.com",
    val selectedSocialPreset: String? = null,
    
    // Wi-Fi
    val wifiSsid: String = "Office_WiFi_5G",
    val wifiPassword: String = "UltraSecureKey2026",
    val wifiAuthType: String = "WPA", // WPA, WEP, nopass
    val wifiIsHidden: Boolean = false,
    
    // Contact (vCard)
    val contactFirstName: String = "Alex",
    val contactLastName: String = "Morgan",
    val contactPhone: String = "+1 (555) 234-5678",
    val contactEmail: String = "alex.morgan@example.com",
    val contactCompany: String = "Apex Tech Innovations",
    val contactJobTitle: String = "Product Architect",
    val contactWebsite: String = "https://apextech.io",
    val contactCity: String = "San Francisco",
    val contactCountry: String = "USA",
    val contactNotes: String = "Met at Tech Summit 2026",
    
    // Email
    val emailTo: String = "contact@example.com",
    val emailSubject: String = "Collaboration Inquiry",
    val emailBody: String = "Hi, I would love to connect regarding your project!",
    
    // Phone & SMS
    val phoneNumber: String = "+1 (555) 019-2834",
    val smsMessage: String = "Hello! Please confirm our scheduled appointment.",
    
    // Document
    val docFileName: String = "Project_Pitch_Deck.pdf",
    val docFileType: String = "PDF", // PDF, IMAGE, VIDEO, DOCX, PPTX, XLSX
    val docFileSize: Long = 2_450_000L,
    val docShareUrl: String = "https://docs.apex.io/view/pitch-deck-2026",
    val docNotes: String = "High priority confidential slide deck",
    val docSelectedUri: Uri? = null,
    val isDocHostedOnline: Boolean = false,
    val docHostedExpiresAt: Long? = null,
    val docHostedDirectUrl: String? = null,
    
    // Calendar Event
    val calTitle: String = "Q3 Product Strategy Summit",
    val calLocation: String = "Conference Hall B & Virtual",
    val calDescription: String = "Review roadmap, offline privacy architecture & 2026 milestones",
    val calStart: String = "20260915T100000Z",
    val calEnd: String = "20260915T113000Z",
    val calStartDisplay: String = "Sep 15, 2026 10:00 AM",
    val calEndDisplay: String = "Sep 15, 2026 11:30 AM",
    
    // Location
    val locLatitude: Double = 37.7749,
    val locLongitude: Double = -122.4194,
    val locLabel: String = "San Francisco, CA",
    
    // Dynamic QR
    val dynTitle: String = "VIP Marketing Campaign 2026",
    val dynTargetUrl: String = "https://example.com/summer-promo",
    val dynShortId: String = "dyn_9x7k2q"
)

class QrViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QrRepository
    val firebaseAuthManager = com.example.qr.firebase.FirebaseAuthManager(application)
    val firestoreManager = com.example.qr.firebase.FirebaseFirestoreManager(application)
    val firebaseStorageManager = com.example.qr.firebase.FirebaseStorageManager(application)
    val freeCloudUploadManager = com.example.qr.engine.FreeCloudUploadManager(application)

    val authState = firebaseAuthManager.authState
    val currentUserProfile = firebaseAuthManager.currentUserProfile

    // Cloud upload state for device files (Firebase Storage)
    private val _cloudUploadState = MutableStateFlow<com.example.qr.engine.CloudUploadState>(com.example.qr.engine.CloudUploadState.Idle)
    val cloudUploadState: StateFlow<com.example.qr.engine.CloudUploadState> = _cloudUploadState.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = QrRepository(db.qrDao())

        // Background purge expired files
        viewModelScope.launch(Dispatchers.IO) {
            repository.purgeExpiredHostedFiles()
        }
    }

    // Hosted 24-Hour Files from Room DB
    val allHostedFiles: StateFlow<List<com.example.qr.data.HostedFileEntity>> = repository.allHostedFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Navigation Screen (0: Generator, 1: Dynamic QR, 2: History & Analytics, 3: Scanner/Decoder)
    private val _currentNavIndex = MutableStateFlow(0)
    val currentNavIndex: StateFlow<Int> = _currentNavIndex.asStateFlow()

    fun setNavIndex(index: Int) {
        _currentNavIndex.value = index
    }

    // Generator Form State
    private val _formState = MutableStateFlow(QrGeneratorFormState())
    val formState: StateFlow<QrGeneratorFormState> = _formState.asStateFlow()

    // Customization Settings
    private val _customization = MutableStateFlow(
        QrCustomization(
            foregroundColor = 0xFF0F172A.toInt(),
            backgroundColor = 0xFFFFFFFF.toInt(),
            isGradient = false,
            gradientColorEnd = 0xFF2563EB.toInt(),
            dotStyle = DotStyle.SQUARE,
            cornerStyle = CornerStyle.ROUNDED,
            errorCorrection = ErrorCorrection.HIGH,
            logoBadge = LogoBadge.NONE
        )
    )
    val customization: StateFlow<QrCustomization> = _customization.asStateFlow()

    // Live Rendered Bitmap
    private val _generatedBitmap = MutableStateFlow<Bitmap?>(null)
    val generatedBitmap: StateFlow<Bitmap?> = _generatedBitmap.asStateFlow()

    // Current Computed Payload String
    private val _currentPayload = MutableStateFlow("")
    val currentPayload: StateFlow<String> = _currentPayload.asStateFlow()

    // Toast/Snackbar notifications
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Data from Room DB
    val allQrCodes: StateFlow<List<QrEntity>> = repository.allQrCodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dynamicQrCodes: StateFlow<List<QrEntity>> = repository.dynamicQrCodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentScanLogs: StateFlow<List<QrScanLogEntity>> = repository.recentScanLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isQrStale = MutableStateFlow(false)
    val isQrStale: StateFlow<Boolean> = _isQrStale.asStateFlow()

    val totalScansCount: StateFlow<Int> = repository.totalScansCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Initial computation on startup
        generateQrNow(showToast = false)
    }

    fun updateForm(transform: (QrGeneratorFormState) -> QrGeneratorFormState) {
        _formState.value = transform(_formState.value)
        _isQrStale.value = true
    }

    fun updateCustomization(transform: (QrCustomization) -> QrCustomization) {
        _customization.value = transform(_customization.value)
        _isQrStale.value = true
    }

    fun updateCustomizationDirect(newCustomization: QrCustomization) {
        _customization.value = newCustomization
        _isQrStale.value = true
    }

    fun selectPayloadType(type: QrPayloadType) {
        _formState.value = _formState.value.copy(selectedType = type)
        _isQrStale.value = true
    }

    fun generateQrNow(showToast: Boolean = true) {
        val form = _formState.value
        val payload = when (form.selectedType) {
            QrPayloadType.TEXT -> QrPayloadBuilder.buildText(form.textContent)
            QrPayloadType.URL -> QrPayloadBuilder.buildUrl(form.urlContent)
            QrPayloadType.WIFI -> QrPayloadBuilder.buildWifi(
                form.wifiSsid,
                form.wifiPassword,
                form.wifiAuthType,
                form.wifiIsHidden
            )
            QrPayloadType.CONTACT -> QrPayloadBuilder.buildVCard(
                firstName = form.contactFirstName,
                lastName = form.contactLastName,
                phone = form.contactPhone,
                email = form.contactEmail,
                organization = form.contactCompany,
                jobTitle = form.contactJobTitle,
                website = form.contactWebsite,
                city = form.contactCity,
                country = form.contactCountry,
                notes = form.contactNotes
            )
            QrPayloadType.EMAIL -> QrPayloadBuilder.buildEmail(
                to = form.emailTo,
                subject = form.emailSubject,
                body = form.emailBody
            )
            QrPayloadType.PHONE -> QrPayloadBuilder.buildPhone(form.phoneNumber)
            QrPayloadType.SMS -> QrPayloadBuilder.buildSms(form.phoneNumber, form.smsMessage)
            QrPayloadType.DOCUMENT -> {
                val targetUrl = if (form.docShareUrl.isNotBlank()) {
                    form.docShareUrl
                } else if (!form.docHostedDirectUrl.isNullOrBlank()) {
                    form.docHostedDirectUrl
                } else {
                    ""
                }
                QrPayloadBuilder.buildDocumentShare(
                    fileName = form.docFileName,
                    fileType = form.docFileType,
                    fileSizeBytes = form.docFileSize,
                    shareUrlOrContent = targetUrl,
                    notes = form.docNotes
                )
            }
            QrPayloadType.CALENDAR -> QrPayloadBuilder.buildCalendarEvent(
                title = form.calTitle,
                description = form.calDescription,
                location = form.calLocation,
                startDateTimeUtc = form.calStart,
                endDateTimeUtc = form.calEnd
            )
            QrPayloadType.LOCATION -> QrPayloadBuilder.buildGeoLocation(
                latitude = form.locLatitude,
                longitude = form.locLongitude,
                label = form.locLabel
            )
        }

        _currentPayload.value = payload

        if (payload.isBlank() && form.selectedType == QrPayloadType.DOCUMENT) {
            viewModelScope.launch {
                _toastEvent.emit("Please upload a file or enter a document URL first.")
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            if (payload.isBlank()) {
                _generatedBitmap.value = null
                _isQrStale.value = false
                return@launch
            }
            val bmp = QrCodeGenerator.generateQrBitmap(
                content = payload,
                size = 900,
                customization = _customization.value,
                context = getApplication()
            )
            _generatedBitmap.value = bmp
            _isQrStale.value = false
            if (showToast) {
                _toastEvent.emit("✨ QR Code generated successfully!")
            }
        }
    }

    private fun regenerateBitmap() {
        generateQrNow(showToast = false)
    }

    fun saveQrToGallery(title: String = "MyQRCode") {
        val bitmap = _generatedBitmap.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val result = QrMediaHelper.saveBitmapToGallery(getApplication(), bitmap, title)
            if (result.isSuccess) {
                _toastEvent.emit("Saved to Photos Gallery (Pictures/QR_Generator)")
                // Auto-save entry in Room History
                saveCurrentQrToHistory(title)
            } else {
                _toastEvent.emit("Failed to save: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun shareCurrentQr(title: String = "QR Code") {
        val bitmap = _generatedBitmap.value ?: return
        QrMediaHelper.shareBitmap(getApplication(), bitmap, title)
    }

    fun copyCurrentPayload() {
        val payload = _currentPayload.value
        if (payload.isNotEmpty()) {
            QrMediaHelper.copyToClipboard(getApplication(), payload)
            viewModelScope.launch {
                _toastEvent.emit("Copied payload to clipboard")
            }
        }
    }

    fun saveCurrentQrToHistory(customTitle: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val form = _formState.value
            val cust = _customization.value
            val title = customTitle?.takeIf { it.isNotBlank() } ?: when (form.selectedType) {
                QrPayloadType.TEXT -> form.textContent.take(24)
                QrPayloadType.URL -> form.urlContent.removePrefix("https://").removePrefix("http://").take(24)
                QrPayloadType.WIFI -> "Wi-Fi: ${form.wifiSsid}"
                QrPayloadType.CONTACT -> "Contact: ${form.contactFirstName} ${form.contactLastName}".trim()
                QrPayloadType.EMAIL -> "Email: ${form.emailTo}"
                QrPayloadType.PHONE -> "Phone: ${form.phoneNumber}"
                QrPayloadType.SMS -> "SMS: ${form.phoneNumber}"
                QrPayloadType.DOCUMENT -> "Doc: ${form.docFileName}"
                QrPayloadType.CALENDAR -> "Event: ${form.calTitle}"
                QrPayloadType.LOCATION -> "Map: ${form.locLabel}"
            }

            val entity = QrEntity(
                title = title,
                type = form.selectedType.name,
                rawInput = _currentPayload.value,
                payload = _currentPayload.value,
                fgColor = cust.foregroundColor,
                bgColor = cust.backgroundColor,
                isGradient = cust.isGradient,
                gradientColorEnd = cust.gradientColorEnd,
                dotStyle = cust.dotStyle.name,
                cornerStyle = cust.cornerStyle.name,
                errorCorrection = cust.errorCorrection.name,
                logoBadge = cust.logoBadge.name,
                customLogoUri = cust.customLogoUri,
                isDynamic = false,
                dynamicId = null,
                dynamicTargetUrl = null,
                scanCount = 0,
                createdAt = System.currentTimeMillis()
            )
            val id = repository.insertQr(entity)
            val savedEntity = entity.copy(id = id)
            val user = currentUserProfile.value
            if (user != null) {
                firestoreManager.saveQrCode(user.uid, savedEntity)
            }
            _toastEvent.emit("Saved to QR Library & Firebase")
        }
    }

    // Analytics & Simulation
    fun simulateScan(qrId: Long, simulatedCity: String, country: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordScan(qrId, simulatedCity, country)
            _toastEvent.emit("Simulated scan recorded from $simulatedCity, $country!")
        }
    }

    fun deleteQr(qr: QrEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteQr(qr)
            _toastEvent.emit("Deleted QR Code")
        }
    }

    fun toggleFavorite(qr: QrEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavorite(qr.id, qr.isFavorite)
        }
    }

    fun loadQrIntoGenerator(qr: QrEntity) {
        _customization.value = QrCustomization(
            foregroundColor = qr.fgColor,
            backgroundColor = qr.bgColor,
            isGradient = qr.isGradient,
            gradientColorEnd = qr.gradientColorEnd,
            dotStyle = DotStyle.valueOf(qr.dotStyle),
            cornerStyle = CornerStyle.valueOf(qr.cornerStyle),
            errorCorrection = ErrorCorrection.valueOf(qr.errorCorrection),
            logoBadge = LogoBadge.valueOf(qr.logoBadge),
            customLogoUri = qr.customLogoUri
        )
        val type = try {
            QrPayloadType.valueOf(qr.type)
        } catch (_: Exception) {
            QrPayloadType.TEXT
        }

        _formState.value = _formState.value.copy(
            selectedType = type,
            textContent = qr.rawInput,
            urlContent = qr.rawInput,
            dynTitle = qr.title,
            dynTargetUrl = qr.dynamicTargetUrl ?: qr.rawInput,
            dynShortId = qr.dynamicId ?: "dyn_custom"
        )
        _currentPayload.value = qr.payload
        _currentNavIndex.value = 0
        regenerateBitmap()
        viewModelScope.launch {
            _toastEvent.emit("Loaded '${qr.title}' into Generator")
        }
    }

    fun setFileShareDoc(name: String, type: String, sizeBytes: Long, shareUrl: String) {
        _formState.value = _formState.value.copy(
            docFileName = name,
            docFileType = type,
            docFileSize = sizeBytes,
            docShareUrl = shareUrl
        )
        generateQrNow(showToast = true)
    }

    // Firebase Cloud Document Upload for On-Device Files (Ad-free, safe, direct)
    fun selectDeviceFile(uri: Uri) {
        val (name, size) = firebaseStorageManager.getFileNameAndSize(uri)
        val ext = name.substringAfterLast('.', "").uppercase()
        val fileType = when (ext) {
            "PDF" -> "PDF"
            "JPG", "JPEG", "PNG", "WEBP", "GIF" -> "IMAGE"
            "MP4", "MKV", "MOV", "AVI" -> "VIDEO"
            "DOC", "DOCX" -> "DOCX"
            "XLS", "XLSX", "CSV" -> "XLSX"
            "PPT", "PPTX" -> "PPTX"
            else -> "DOCUMENT"
        }
        _formState.value = _formState.value.copy(
            docFileName = name,
            docFileType = fileType,
            docFileSize = size,
            docSelectedUri = uri,
            isDocHostedOnline = false,
            docHostedExpiresAt = null,
            docHostedDirectUrl = null
        )
        _isQrStale.value = true
    }

    fun uploadSelectedFileToCloud() {
        val uri = _formState.value.docSelectedUri ?: return
        val fileName = _formState.value.docFileName
        viewModelScope.launch {
            _cloudUploadState.value = com.example.qr.engine.CloudUploadState.Uploading(10, fileName)
            val user = currentUserProfile.value
            val userId = user?.uid ?: "guest_user"

            // Upload using free, safe direct file cloud uploader (No credit card / No Blaze required)
            val result = freeCloudUploadManager.uploadFile(uri) { progress ->
                _cloudUploadState.value = com.example.qr.engine.CloudUploadState.Uploading(progress, fileName)
            }

            if (result.isSuccess) {
                val upload = result.getOrThrow()
                _cloudUploadState.value = com.example.qr.engine.CloudUploadState.Success(upload)
                
                // Save to Room DB
                val expiryTime = upload.expiresAt ?: (System.currentTimeMillis() + (72L * 60 * 60 * 1000L))
                val entity = com.example.qr.data.HostedFileEntity(
                    fileName = upload.fileName,
                    fileSizeBytes = upload.fileSizeBytes,
                    fileMimeType = upload.mimeType,
                    downloadUrl = upload.directDownloadUrl,
                    uploadedAt = System.currentTimeMillis(),
                    expiresAt = expiryTime,
                    status = "ACTIVE",
                    localUriString = uri.toString()
                )
                val id = repository.insertHostedFile(entity)
                
                // Save metadata record to Firebase Firestore (text metadata is 100% free)
                firestoreManager.saveHostedFile(userId, entity.copy(id = id))

                // Update generator form with direct public URL
                _formState.value = _formState.value.copy(
                    docShareUrl = upload.directDownloadUrl,
                    isDocHostedOnline = true,
                    docHostedExpiresAt = expiryTime,
                    docHostedDirectUrl = upload.directDownloadUrl
                )
                generateQrNow(showToast = false)
                _toastEvent.emit("✓ File uploaded! Direct & safe QR code ready.")
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Upload failed. Please check internet connection."
                _cloudUploadState.value = com.example.qr.engine.CloudUploadState.Error(errorMsg)
                _toastEvent.emit("Upload error: $errorMsg")
            }
        }
    }

    fun deleteHostedFile(file: com.example.qr.data.HostedFileEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHostedFileById(file.id)
            val user = currentUserProfile.value
            val userId = user?.uid ?: "guest_user"
            firestoreManager.deleteHostedFile(userId, file.id)
            _toastEvent.emit("Removed ${file.fileName} from Firebase & local list")
        }
    }

    fun loadHostedFileIntoGenerator(file: com.example.qr.data.HostedFileEntity) {
        val ext = file.fileName.substringAfterLast('.', "").uppercase()
        val fileType = when (ext) {
            "PDF" -> "PDF"
            "JPG", "JPEG", "PNG", "WEBP" -> "IMAGE"
            "MP4", "MKV" -> "VIDEO"
            "DOC", "DOCX" -> "DOCX"
            "XLS", "XLSX" -> "XLSX"
            "PPT", "PPTX" -> "PPTX"
            else -> "DOCUMENT"
        }
        _formState.value = _formState.value.copy(
            selectedType = QrPayloadType.DOCUMENT,
            docFileName = file.fileName,
            docFileType = fileType,
            docFileSize = file.fileSizeBytes,
            docShareUrl = file.downloadUrl,
            isDocHostedOnline = true,
            docHostedExpiresAt = file.expiresAt,
            docHostedDirectUrl = file.downloadUrl
        )
        _currentNavIndex.value = 0
        generateQrNow(showToast = false)
        viewModelScope.launch {
            _toastEvent.emit("Loaded '${file.fileName}' into Generator")
        }
    }

    // Firebase Authentication Actions
    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _toastEvent.emit("Connecting to Google Sign-In...")
            val result = firebaseAuthManager.signInWithGoogle(activityContext)
            if (result.isSuccess) {
                val profile = result.getOrThrow()
                _toastEvent.emit("Signed in as ${profile.displayName} (${profile.email})")
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Google Sign-In cancelled"
                _toastEvent.emit(msg)
            }
        }
    }

    fun signInDirect(name: String, email: String) {
        viewModelScope.launch {
            val result = firebaseAuthManager.signInDirectUser(name, email)
            if (result.isSuccess) {
                val profile = result.getOrThrow()
                _toastEvent.emit("Welcome ${profile.displayName}!")
            } else {
                _toastEvent.emit("Sign in failed")
            }
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            firebaseAuthManager.signOut(context)
            _toastEvent.emit("Signed out of Firebase")
        }
    }
}
