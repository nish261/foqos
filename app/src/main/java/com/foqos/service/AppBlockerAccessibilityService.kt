package com.foqos.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.foqos.data.repository.SessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Accessibility Service for app blocking functionality.
 * 
 * This service monitors app launches and blocks access to apps that are
 * configured in an active blocking profile. When a blocked app is detected,
 * it returns the user to the home screen.
 * 
 * NOTE: User must manually enable this service in Settings > Accessibility
 */
@AndroidEntryPoint
class AppBlockerAccessibilityService : AccessibilityService() {
    
    @Inject
    lateinit var sessionRepository: SessionRepository
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var blockedApps: Set<String> = emptySet()
    private var isActive = false
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        // Service is now active and monitoring
        observeActiveSession()
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !isActive) return
        
        // Only monitor window state changes (app switches)
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Don't block Foqos itself or launcher/system apps
            if (packageName == "com.foqos" || 
                packageName.startsWith("com.android.launcher") ||
                packageName.startsWith("com.google.android.googlequicksearchbox")) {
                return
            }
            
            // Check if this app is blocked
            if (blockedApps.contains(packageName)) {
                blockApp(packageName)
            }
        }
    }
    
    override fun onInterrupt() {
        // Service interrupted, cleanup if needed
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    
    /**
     * Observe active session and update blocked apps list.
     */
    private fun observeActiveSession() {
        serviceScope.launch {
            sessionRepository.getActiveSessionFlow().collect { session ->
                if (session != null) {
                    blockedApps = session.blockedApps.toSet()
                    isActive = true
                } else {
                    blockedApps = emptySet()
                    isActive = false
                }
            }
        }
    }
    
    /**
     * Block the app by showing overlay and returning to home screen.
     */
    private fun blockApp(packageName: String) {
        // Show blocking overlay with app info
        serviceScope.launch {
            val appInfo = try {
                val pm = packageManager
                val info = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(info).toString()
            } catch (e: Exception) {
                packageName
            }
            
            // Get active profile name
            val session = sessionRepository.getActiveSession()
            val profileName = session?.let {
                // You would get profile name from repository here
                "Active Profile"
            } ?: "Focus Session"
            
            // Show custom overlay
            com.foqos.presentation.blocking.BlockingOverlayService.show(
                this@AppBlockerAccessibilityService,
                appInfo,
                profileName
            )
        }
        
        // Also go to home screen
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }
}
