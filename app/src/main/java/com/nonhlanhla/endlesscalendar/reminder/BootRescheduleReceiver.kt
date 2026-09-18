package com.nonhlanhla.endlesscalendar.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nonhlanhla.endlesscalendar.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val events = AppDatabase.getInstance(context).eventDao().getAllOnce()
                ReminderScheduler.rescheduleAll(context, events)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
