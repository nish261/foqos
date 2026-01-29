package com.foqos.domain.model

sealed interface BlockingStrategy {
    val id: String
    val name: String
    val description: String
    val iconType: String
    
    fun requiresNFC(): Boolean = false
    fun requiresQR(): Boolean = false
    fun requiresTimer(): Boolean = false
    fun manualStart(): Boolean = false
    
    object NFCBlocking : BlockingStrategy {
        override val id = "nfc"
        override val name = "NFC Tag"
        override val description = "Start and stop with NFC tag tap"
        override val iconType = "nfc"
        override fun requiresNFC() = true
    }
    
    object QRCodeBlocking : BlockingStrategy {
        override val id = "qr"
        override val name = "QR Code"
        override val description = "Start and stop with QR code scan"
        override val iconType = "qr"
        override fun requiresQR() = true
    }
    
    object ManualBlocking : BlockingStrategy {
        override val id = "manual"
        override val name = "Manual"
        override val description = "Start and stop manually in the app"
        override val iconType = "manual"
        override fun manualStart() = true
    }
    
    object NFCManualBlocking : BlockingStrategy {
        override val id = "nfc_manual"
        override val name = "NFC + Manual"
        override val description = "Start manually, stop with NFC tag"
        override val iconType = "nfc_manual"
        override fun requiresNFC() = true
        override fun manualStart() = true
    }
    
    object QRManualBlocking : BlockingStrategy {
        override val id = "qr_manual"
        override val name = "QR + Manual"
        override val description = "Start manually, stop with QR code"
        override val iconType = "qr_manual"
        override fun requiresQR() = true
        override fun manualStart() = true
    }
    
    object NFCTimerBlocking : BlockingStrategy {
        override val id = "nfc_timer"
        override val name = "NFC + Timer"
        override val description = "Start with timer, stop early with NFC tag"
        override val iconType = "nfc_timer"
        override fun requiresNFC() = true
        override fun requiresTimer() = true
        override fun manualStart() = true
    }
    
    object QRTimerBlocking : BlockingStrategy {
        override val id = "qr_timer"
        override val name = "QR + Timer"
        override val description = "Start with timer, stop early with QR code"
        override val iconType = "qr_timer"
        override fun requiresQR() = true
        override fun requiresTimer() = true
        override fun manualStart() = true
    }
    
    companion object {
        fun getAll(): List<BlockingStrategy> = listOf(
            NFCBlocking,
            QRCodeBlocking,
            ManualBlocking,
            NFCManualBlocking,
            QRManualBlocking,
            NFCTimerBlocking,
            QRTimerBlocking
        )
        
        fun fromId(id: String): BlockingStrategy = when (id) {
            "nfc" -> NFCBlocking
            "qr" -> QRCodeBlocking
            "manual" -> ManualBlocking
            "nfc_manual" -> NFCManualBlocking
            "qr_manual" -> QRManualBlocking
            "nfc_timer" -> NFCTimerBlocking
            "qr_timer" -> QRTimerBlocking
            else -> ManualBlocking
        }
    }
}
