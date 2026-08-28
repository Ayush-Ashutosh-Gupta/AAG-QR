package com.example.qr.firebase

import android.content.Context
import android.util.Log
import com.example.qr.data.HostedFileEntity
import com.example.qr.data.QrEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class FirestoreDynamicLink(
    val dynamicId: String = "",
    val title: String = "",
    val targetUrl: String = "",
    val userId: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val totalScans: Long = 0
)

data class FirestoreHostedDoc(
    val id: String = "",
    val fileName: String = "",
    val fileSizeBytes: Long = 0,
    val mimeType: String = "",
    val downloadUrl: String = "",
    val uploadedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000L),
    val status: String = "ACTIVE",
    val userId: String = ""
)

class FirebaseFirestoreManager(private val context: Context) {

    private val tag = "FirestoreManager"
    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
            db
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize Firestore: ${e.message}")
            null
        }
    }

    // Save QR to Firestore
    suspend fun saveQrCode(userId: String, qr: QrEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext Result.success(Unit)
            val docRef = db.collection("users").document(userId)
                .collection("qr_codes").document(qr.id.toString())

            val data = hashMapOf(
                "id" to qr.id,
                "title" to qr.title,
                "payload" to qr.payload,
                "type" to qr.type,
                "createdAt" to qr.createdAt,
                "isDynamic" to qr.isDynamic,
                "dynamicId" to (qr.dynamicId ?: ""),
                "dynamicTargetUrl" to (qr.dynamicTargetUrl ?: ""),
                "scanCount" to qr.scanCount,
                "isFavorite" to qr.isFavorite,
                "dotStyle" to qr.dotStyle,
                "cornerStyle" to qr.cornerStyle,
                "userId" to userId
            )

            docRef.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(tag, "Failed to save QR to Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    // Save or Update Dynamic QR in global collection for instant redirects
    suspend fun saveDynamicQr(userId: String, dynamicId: String, title: String, targetUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext Result.success(Unit)
            val docRef = db.collection("dynamic_qrs").document(dynamicId)
            val data = hashMapOf(
                "dynamicId" to dynamicId,
                "title" to title,
                "targetUrl" to targetUrl,
                "userId" to userId,
                "updatedAt" to System.currentTimeMillis()
            )
            docRef.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(tag, "Failed to save Dynamic QR to Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    // Save Hosted File Record in Firestore
    suspend fun saveHostedFile(userId: String, file: HostedFileEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext Result.success(Unit)
            val docRef = db.collection("users").document(userId)
                .collection("hosted_docs").document(file.id.toString())

            val data = hashMapOf(
                "id" to file.id,
                "fileName" to file.fileName,
                "fileSizeBytes" to file.fileSizeBytes,
                "mimeType" to file.fileMimeType,
                "downloadUrl" to file.downloadUrl,
                "uploadedAt" to file.uploadedAt,
                "expiresAt" to file.expiresAt,
                "status" to file.status,
                "userId" to userId
            )
            docRef.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(tag, "Failed to save hosted file to Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    // Delete Hosted File from Firestore
    suspend fun deleteHostedFile(userId: String, fileId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext Result.success(Unit)
            db.collection("users").document(userId)
                .collection("hosted_docs").document(fileId.toString()).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Real-time flow of user QR codes from Firestore
    fun streamUserQrs(userId: String): Flow<List<FirestoreDynamicLink>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("dynamic_qrs")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FirestoreDynamicLink::class.java)
                    }
                    trySend(list)
                }
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)
}
