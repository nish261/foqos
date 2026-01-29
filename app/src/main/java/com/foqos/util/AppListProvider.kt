package com.foqos.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper for getting installed apps on device.
 * Provides app list for selection UI.
 */
@Singleton
class AppListProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Get all user-installed apps (excludes system apps).
     */
    suspend fun getUserApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        
        packages
            .filter { app ->
                // Filter out system apps
                (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
            }
            .map { app ->
                AppInfo(
                    packageName = app.packageName,
                    appName = app.loadLabel(packageManager).toString(),
                    icon = app.loadIcon(packageManager)
                )
            }
            .sortedBy { it.appName.lowercase() }
    }
    
    /**
     * Get all apps including system apps.
     */
    suspend fun getAllApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        
        packages
            .map { app ->
                AppInfo(
                    packageName = app.packageName,
                    appName = app.loadLabel(packageManager).toString(),
                    icon = app.loadIcon(packageManager)
                )
            }
            .sortedBy { it.appName.lowercase() }
    }
    
    /**
     * Get app info by package name.
     */
    fun getAppInfo(packageName: String): AppInfo? {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            AppInfo(
                packageName = packageName,
                appName = appInfo.loadLabel(packageManager).toString(),
                icon = appInfo.loadIcon(packageManager)
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
    
    /**
     * Common distracting apps to suggest.
     */
    fun getSuggestedApps(): List<String> = listOf(
        "com.instagram.android",
        "com.twitter.android",
        "com.facebook.katana",
        "com.snapchat.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.reddit.frontpage",
        "com.netflix.mediaclient",
        "com.spotify.music",
        "com.google.android.youtube"
    )
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable
)
