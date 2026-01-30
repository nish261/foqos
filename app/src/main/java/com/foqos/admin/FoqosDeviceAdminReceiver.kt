package com.foqos.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Device Admin Receiver for Strict Mode enforcement
 *
 * When activated, this prevents:
 * - App uninstallation while a strict mode session is active
 * - Disabling the app from Settings
 * - Force stopping the app
 */
class FoqosDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(
            context,
            "Strict Mode enabled. App cannot be uninstalled during sessions.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(
            context,
            "Strict Mode disabled. App can now be uninstalled.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Disabling Strict Mode will allow you to uninstall the app and bypass focus sessions. Are you sure?"
    }

    companion object {
        /**
         * Check if device admin is active
         */
        fun isAdminActive(context: Context): Boolean {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE)
                as android.app.admin.DevicePolicyManager
            val componentName = android.content.ComponentName(
                context,
                FoqosDeviceAdminReceiver::class.java
            )
            return devicePolicyManager.isAdminActive(componentName)
        }

        /**
         * Request device admin activation
         */
        fun requestAdminActivation(context: Context) {
            if (!isAdminActive(context)) {
                val intent = Intent(android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                val componentName = android.content.ComponentName(
                    context,
                    FoqosDeviceAdminReceiver::class.java
                )
                intent.putExtra(
                    android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    componentName
                )
                intent.putExtra(
                    android.app.admin.DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Foqos needs Device Admin permission to enforce Strict Mode during focus sessions. " +
                    "This prevents you from uninstalling the app or stopping services while a session is active."
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }
}
