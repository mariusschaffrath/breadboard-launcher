package me.jxl.kiosk_satellite

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Locale

/**
 * Backward compatibility helper for accessing Locale configuration across Android versions.
 * Configuration.getLocales() was added in API 24 (Android 7.0). On API <= 23 (such as Fire OS 5.1.1 / API 22),
 * accessing Configuration.locales causes NoSuchMethodError: Configuration.getLocales().
 */
object LocaleCompat {
    fun getPrimaryLocale(context: Context): Locale {
        return getPrimaryLocale(context.resources.configuration)
    }

    fun getPrimaryLocale(configuration: Configuration): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Api24Impl.getPrimaryLocale(configuration)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale ?: Locale.getDefault()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private object Api24Impl {
        fun getPrimaryLocale(configuration: Configuration): Locale {
            return configuration.locales[0]
        }
    }
}
