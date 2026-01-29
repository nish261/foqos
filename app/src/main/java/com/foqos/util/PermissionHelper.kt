package com.foqos.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper for managing app permissions.
 */
@Singleton
class PermissionHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Check if accessibility service is enabled.
     */
    fun isAccessibilityServiceEnabled(): Boolean {
        val settingValue = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return settingValue?.contains("com.foqos") == true
    }
    
    /**
     * Open accessibility settings.
     */
    fun openAccessibilitySettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        activity.startActivity(intent)
    }
    
    /**
     * Check if usage stats permission is granted.
     */
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }
    
    /**
     * Open usage stats settings.
     */
    fun openUsageStatsSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        activity.startActivity(intent)
    }
    
    /**
     * Check if VPN permission is granted.
     */
    fun hasVpnPermission(): Boolean {
        val intent = VpnService.prepare(context)
        return intent == null
    }
    
    /**
     * Request VPN permission.
     */
    fun requestVpnPermission(launcher: ActivityResultLauncher<Intent>) {
        val intent = VpnService.prepare(context)
        if (intent != null) {
            launcher.launch(intent)
        }
    }
    
    /**
     * Check if overlay permission is granted.
     */
    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }
    
    /**
     * Open overlay permission settings.
     */
    fun openOverlaySettings(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}")
        )
        activity.startActivity(intent)
    }
    
    /**
     * Check if all critical permissions are granted.
     */
    fun hasAllCriticalPermissions(): Boolean {
        return isAccessibilityServiceEnabled() &&
                hasUsageStatsPermission() &&
                hasOverlayPermission()
    }
}
