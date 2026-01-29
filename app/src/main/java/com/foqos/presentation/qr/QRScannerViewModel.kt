package com.foqos.presentation.qr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foqos.qr.QRActionHandler
import com.foqos.qr.QRActionResult
import com.foqos.qr.QRScanner
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QRScannerViewModel @Inject constructor(
    private val qrScanner: QRScanner,
    private val qrActionHandler: QRActionHandler
) : ViewModel() {

    private val _scanState = MutableStateFlow<QRScanState>(QRScanState.Scanning)
    val scanState: StateFlow<QRScanState> = _scanState.asStateFlow()

    private var isProcessing = false

    init {
        // Listen for QR action results
        viewModelScope.launch {
            qrActionHandler.actionResult.collect { result ->
                when (result) {
                    is QRActionResult.Success -> {
                        _scanState.value = QRScanState.Success(result.message)
                    }
                    is QRActionResult.Error -> {
                        _scanState.value = QRScanState.Error(result.message)
                    }
                }
                isProcessing = false
            }
        }
    }

    fun processImage(image: InputImage) {
        if (isProcessing) return
        if (_scanState.value !is QRScanState.Scanning) return

        isProcessing = true

        viewModelScope.launch {
            when (val result = qrScanner.scanImage(image)) {
                is QRScanner.ScanResult.ProfileFound -> {
                    // Process the QR code with QRActionHandler
                    qrActionHandler.handleQRCode(result.rawContent)
                }
                is QRScanner.ScanResult.GenericCode -> {
                    // Try to process generic code
                    qrActionHandler.handleQRCode(result.content)
                }
                is QRScanner.ScanResult.NoCodeFound -> {
                    isProcessing = false
                }
                is QRScanner.ScanResult.Error -> {
                    _scanState.value = QRScanState.Error(result.message)
                    isProcessing = false
                }
            }
        }
    }

    fun onError(message: String) {
        _scanState.value = QRScanState.Error(message)
        isProcessing = false
    }

    fun resetToScanning() {
        _scanState.value = QRScanState.Scanning
        isProcessing = false
    }

    override fun onCleared() {
        super.onCleared()
        qrScanner.close()
    }
}
