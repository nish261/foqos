package com.foqos.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for app preferences and first-run state.
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "foqos_prefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_ACCESSIBILITY_PROMPTED = "accessibility_prompted"
        private const val KEY_USAGE_STATS_PROMPTED = "usage_stats_prompted"
    }
    
    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()
    
    var hasPromptedAccessibility: Boolean
        get() = prefs.getBoolean(KEY_ACCESSIBILITY_PROMPTED, false)
        set(value) = prefs.edit().putBoolean(KEY_ACCESSIBILITY_PROMPTED, value).apply()
    
    var hasPromptedUsageStats: Boolean
        get() = prefs.getBoolean(KEY_USAGE_STATS_PROMPTED, false)
        set(value) = prefs.edit().putBoolean(KEY_USAGE_STATS_PROMPTED, value).apply()
}
