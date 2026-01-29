package com.foqos.qr

import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scanner for QR codes using ML Kit.
 * Integrates with CameraX for live scanning.
 */
@Singleton
class QRScanner @Inject constructor() {
    
    companion object {
        private const val TAG = "QRScanner"
    }
    
    private val scanner = BarcodeScanning.getClient()
    
    /**
     * Scan an image for QR codes.
     * Returns the first QR code found.
     */
    suspend fun scanImage(image: InputImage): ScanResult {
        return try {
            val barcodes = scanner.process(image).await()
            
            if (barcodes.isEmpty()) {
                return ScanResult.NoCodeFound
            }
            
            val qrCode = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
            
            if (qrCode == null) {
                return ScanResult.NoCodeFound
            }
            
            val rawValue = qrCode.rawValue
            if (rawValue.isNullOrBlank()) {
                return ScanResult.NoCodeFound
            }
            
            // Check if it's a Foqos profile deep link
            if (rawValue.startsWith("https://foqos.app/profile/") ||
                rawValue.startsWith("foqos://profile/")) {
                val profileId = extractProfileId(rawValue)
                if (profileId != null) {
                    return ScanResult.ProfileFound(profileId, rawValue)
                }
            }
            
            // Generic QR code (can still be used for QR blocking strategies)
            ScanResult.GenericCode(rawValue)
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning QR code", e)
            ScanResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Extract profile ID from deep link.
     */
    private fun extractProfileId(deepLink: String): String? {
        return try {
            // https://foqos.app/profile/{UUID}
            // or foqos://profile/{UUID}
            val parts = deepLink.split("/profile/")
            if (parts.size == 2) {
                parts[1].trim()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Close the scanner and release resources.
     */
    fun close() {
        scanner.close()
    }
    
    sealed class ScanResult {
        data class ProfileFound(val profileId: String, val rawContent: String) : ScanResult()
        data class GenericCode(val content: String) : ScanResult()
        object NoCodeFound : ScanResult()
        data class Error(val message: String) : ScanResult()
    }
}
