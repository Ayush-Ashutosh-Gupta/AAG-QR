package com.example.qr.data

import kotlinx.coroutines.flow.Flow

class QrRepository(private val qrDao: QrDao) {

    val allQrCodes: Flow<List<QrEntity>> = qrDao.getAllQrCodes()
    val dynamicQrCodes: Flow<List<QrEntity>> = qrDao.getDynamicQrCodes()
    val favoriteQrCodes: Flow<List<QrEntity>> = qrDao.getFavoriteQrCodes()
    val recentScanLogs: Flow<List<QrScanLogEntity>> = qrDao.getAllRecentLogs()
    val totalScansCount: Flow<Int> = qrDao.getTotalScansCount()

    suspend fun getQrById(id: Long): QrEntity? = qrDao.getQrById(id)

    suspend fun getQrByDynamicId(dynamicId: String): QrEntity? = qrDao.getQrByDynamicId(dynamicId)

    suspend fun insertQr(qr: QrEntity): Long = qrDao.insertQr(qr)

    suspend fun updateQr(qr: QrEntity) = qrDao.updateQr(qr)

    suspend fun deleteQr(qr: QrEntity) = qrDao.deleteQr(qr)

    suspend fun deleteQrById(id: Long) = qrDao.deleteQrById(id)

    suspend fun toggleFavorite(id: Long, current: Boolean) = qrDao.updateFavorite(id, !current)

    suspend fun updateDynamicTarget(id: Long, targetUrl: String, payload: String) {
        qrDao.updateDynamicTarget(id, targetUrl, payload)
    }

    suspend fun recordScan(qrId: Long, city: String = "Local Device", country: String = "Offline") {
        qrDao.incrementScanCount(qrId)
        qrDao.insertScanLog(
            QrScanLogEntity(
                qrId = qrId,
                scannedAt = System.currentTimeMillis(),
                locationCity = city,
                locationCountry = country
            )
        )
    }

    fun getLogsForQr(qrId: Long): Flow<List<QrScanLogEntity>> = qrDao.getLogsForQr(qrId)

    // 24-Hour Temporary Hosted Files
    val allHostedFiles: Flow<List<HostedFileEntity>> = qrDao.getAllHostedFiles()

    suspend fun insertHostedFile(file: HostedFileEntity): Long = qrDao.insertHostedFile(file)

    suspend fun deleteHostedFileById(id: Long) = qrDao.deleteHostedFileById(id)

    suspend fun purgeExpiredHostedFiles() = qrDao.purgeExpiredHostedFiles()
}
