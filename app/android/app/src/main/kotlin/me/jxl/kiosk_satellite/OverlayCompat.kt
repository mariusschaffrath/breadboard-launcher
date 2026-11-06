package me.jxl.kiosk_satellite

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

/**
 * Backward compatibility helper for checking overlay permissions.
 * Settings.canDrawOverlays(Context) was introduced in API 23 (Android 6.0).
 * Prior to API 23, SYSTEM_ALERT_WINDOW was granted at install time.
 */
object OverlayCompat {
    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Api23Impl.canDrawOverlays(context)
        } else {
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private object Api23Impl {
        fun canDrawOverlays(context: Context): Boolean {
            return Settings.canDrawOverlays(context)
        }
    }
}
