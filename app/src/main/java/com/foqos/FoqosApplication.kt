package com.foqos

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FoqosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize app-wide components if needed
    }
}
