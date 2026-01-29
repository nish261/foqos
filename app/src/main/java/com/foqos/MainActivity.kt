package com.foqos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.foqos.presentation.FoqosApp
import com.foqos.presentation.onboarding.OnboardingScreen
import com.foqos.presentation.theme.FoqosTheme
import com.foqos.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
}
