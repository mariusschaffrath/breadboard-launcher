package me.jxl.kiosk_satellite

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Backward compatibility helper for AlarmManager while-idle alarm scheduling.
 * setAndAllowWhileIdle and setExactAndAllowWhileIdle were added in API 23 (Android 6.0).
 * On API < 23, they fall back to set() and setExact().
 */
object AlarmCompat {
    fun setAndAllowWhileIdle(alarmManager: AlarmManager, type: Int, triggerAtMillis: Long, operation: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Api23Impl.setAndAllowWhileIdle(alarmManager, type, triggerAtMillis, operation)
        } else {
            alarmManager.set(type, triggerAtMillis, operation)
        }
    }

    fun setExactAndAllowWhileIdle(alarmManager: AlarmManager, type: Int, triggerAtMillis: Long, operation: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Api23Impl.setExactAndAllowWhileIdle(alarmManager, type, triggerAtMillis, operation)
        } else {
            alarmManager.setExact(type, triggerAtMillis, operation)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private object Api23Impl {
        fun setAndAllowWhileIdle(alarmManager: AlarmManager, type: Int, triggerAtMillis: Long, operation: PendingIntent) {
            alarmManager.setAndAllowWhileIdle(type, triggerAtMillis, operation)
        }

        fun setExactAndAllowWhileIdle(alarmManager: AlarmManager, type: Int, triggerAtMillis: Long, operation: PendingIntent) {
            alarmManager.setExactAndAllowWhileIdle(type, triggerAtMillis, operation)
        }
    }
}
