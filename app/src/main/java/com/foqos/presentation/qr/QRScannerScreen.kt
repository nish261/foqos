package com.foqos.presentation.qr

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    navController: NavController,
    viewModel: QRScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanState by viewModel.scanState.collectAsState()

    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR Code to Unlock") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = scanState) {
                is QRScanState.Scanning -> {
                    // Camera preview
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val executor = Executors.newSingleThreadExecutor()

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()

                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val imageAnalyzer = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also {
                                        it.setAnalyzer(executor) { imageProxy ->
                                            val mediaImage = imageProxy.image
                                            if (mediaImage != null) {
                                                val image = InputImage.fromMediaImage(
                                                    mediaImage,
                                                    imageProxy.imageInfo.rotationDegrees
                                                )
                                                viewModel.processImage(image)
                                            }
                                            imageProxy.close()
                                        }
                                    }

                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalyzer
                                    )
                                } catch (e: Exception) {
                                    viewModel.onError(e.message ?: "Camera error")
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Scanning overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = "Position QR code within frame",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                is QRScanState.Success -> {
                    // Success state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Session Unlocked!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done")
                        }
                    }
                }

                is QRScanState.Error -> {
                    // Error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "❌",
                            style = MaterialTheme.typography.displayLarge
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Scan Failed",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { viewModel.resetToScanning() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Try Again")
                        }

                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

sealed class QRScanState {
    object Scanning : QRScanState()
    data class Success(val message: String) : QRScanState()
    data class Error(val message: String) : QRScanState()
}
