package com.foqos

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.foqos.nfc.NFCReader
import com.foqos.presentation.FoqosApp
import com.foqos.presentation.onboarding.OnboardingScreen
import com.foqos.presentation.theme.FoqosTheme
import com.foqos.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    @Inject
    lateinit var nfcReader: NFCReader
    
    private var nfcAdapter: NfcAdapter? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        setContent {
            FoqosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showOnboarding by remember {
                        mutableStateOf(!preferencesManager.hasCompletedOnboarding)
                    }
                    
                    if (showOnboarding) {
                        OnboardingScreen(
                            onComplete = {
                                preferencesManager.hasCompletedOnboarding = true
                                showOnboarding = false
                            }
                        )
                    } else {
                        FoqosApp()
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
    }
    
    override fun onPause() {
        super.onPause()
        disableNfcForegroundDispatch()
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }
    
    private fun enableNfcForegroundDispatch() {
        nfcAdapter?.let { adapter ->
            if (adapter.isEnabled) {
                val intent = Intent(this, javaClass).apply {
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                
                // PendingIntent.FLAG_MUTABLE is required for NFC
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_MUTABLE
                } else {
                    0
                }
                
                val pendingIntent = PendingIntent.getActivity(this, 0, intent, flags)
                val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
                
                adapter.enableForegroundDispatch(this, pendingIntent, filters, null)
            }
        }
    }
    
    private fun disableNfcForegroundDispatch() {
        nfcAdapter?.disableForegroundDispatch(this)
    }
    
    private fun handleNfcIntent(intent: Intent?) {
        if (intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            intent?.action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
            intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
            
            val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            
            tag?.let {
                lifecycleScope.launch {
                    nfcReader.emitTag(it)
                }
            }
        }
    }
}

