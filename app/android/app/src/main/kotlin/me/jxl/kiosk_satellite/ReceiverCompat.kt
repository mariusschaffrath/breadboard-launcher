package me.jxl.kiosk_satellite

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build

object ReceiverCompat {
    fun register(
        context: Context,
        receiver: BroadcastReceiver,
        filter: IntentFilter,
        exported: Boolean = false,
    ) {
        if (Build.VERSION.SDK_INT >= 33) {
            context.registerReceiver(
                receiver,
                filter,
                if (exported) Context.RECEIVER_EXPORTED else Context.RECEIVER_NOT_EXPORTED,
            )
        } else {
            context.registerReceiver(receiver, filter)
        }
    }
}
