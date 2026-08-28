package com.example.qr.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QrDao {

    @Query("SELECT * FROM qr_codes ORDER BY createdAt DESC")
    fun getAllQrCodes(): Flow<List<QrEntity>>

    @Query("SELECT * FROM qr_codes WHERE isDynamic = 1 ORDER BY createdAt DESC")
    fun getDynamicQrCodes(): Flow<List<QrEntity>>

    @Query("SELECT * FROM qr_codes WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteQrCodes(): Flow<List<QrEntity>>

    @Query("SELECT * FROM qr_codes WHERE id = :id LIMIT 1")
    suspend fun getQrById(id: Long): QrEntity?

    @Query("SELECT * FROM qr_codes WHERE dynamicId = :dynamicId LIMIT 1")
    suspend fun getQrByDynamicId(dynamicId: String): QrEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQr(qr: QrEntity): Long

    @Update
    suspend fun updateQr(qr: QrEntity)

    @Delete
    suspend fun deleteQr(qr: QrEntity)

    @Query("DELETE FROM qr_codes WHERE id = :id")
    suspend fun deleteQrById(id: Long)

    @Query("UPDATE qr_codes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE qr_codes SET scanCount = scanCount + 1 WHERE id = :id")
    suspend fun incrementScanCount(id: Long)

    @Query("UPDATE qr_codes SET dynamicTargetUrl = :targetUrl, payload = :payload WHERE id = :id")
    suspend fun updateDynamicTarget(id: Long, targetUrl: String, payload: String)

    // Analytics Scan Logs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanLog(log: QrScanLogEntity): Long

    @Query("SELECT * FROM qr_scan_logs WHERE qrId = :qrId ORDER BY scannedAt DESC")
    fun getLogsForQr(qrId: Long): Flow<List<QrScanLogEntity>>

    @Query("SELECT * FROM qr_scan_logs ORDER BY scannedAt DESC LIMIT 100")
    fun getAllRecentLogs(): Flow<List<QrScanLogEntity>>

    @Query("SELECT COUNT(*) FROM qr_scan_logs")
    fun getTotalScansCount(): Flow<Int>

    // 24-Hour Temporary Hosted Files
    @Query("SELECT * FROM hosted_files ORDER BY uploadedAt DESC")
    fun getAllHostedFiles(): Flow<List<HostedFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHostedFile(file: HostedFileEntity): Long

    @Query("DELETE FROM hosted_files WHERE id = :id")
    suspend fun deleteHostedFileById(id: Long)

    @Query("DELETE FROM hosted_files WHERE expiresAt < :currentTime")
    suspend fun purgeExpiredHostedFiles(currentTime: Long = System.currentTimeMillis())
}
