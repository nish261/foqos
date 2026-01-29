package com.foqos.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generator for QR codes containing profile deep links.
 * Uses ZXing library to create QR code images.
 */
@Singleton
class QRGenerator @Inject constructor() {
    
    /**
     * Generate QR code bitmap for a profile.
     * 
     * @param profileId The profile UUID to encode
     * @param size The size in pixels (width and height)
     * @return Bitmap QR code image
     */
    fun generateProfileQR(
        profileId: String,
        size: Int = 512
    ): Bitmap {
        val deepLink = "https://foqos.app/profile/$profileId"
        return generateQRCode(deepLink, size)
    }
    
    /**
     * Generate QR code for any content.
     * 
     * @param content The string to encode in the QR code
     * @param size The size in pixels (width and height)
     * @return Bitmap QR code image
     */
    fun generateQRCode(
        content: String,
        size: Int = 512
    ): Bitmap {
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            put(EncodeHintType.MARGIN, 1)
        }
        
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        
        return bitmap
    }
    
    /**
     * Generate QR code with custom colors.
     */
    fun generateQRCodeWithColors(
        content: String,
        size: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap {
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            put(EncodeHintType.MARGIN, 1)
        }
        
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) foregroundColor else backgroundColor
                )
            }
        }
        
        return bitmap
    }
}
