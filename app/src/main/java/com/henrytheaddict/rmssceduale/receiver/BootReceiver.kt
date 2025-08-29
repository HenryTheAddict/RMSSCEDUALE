package com.henrytheaddict.rmssceduale.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.henrytheaddict.rmssceduale.service.ScheduleMonitorService

/**
 * Broadcast receiver to start the schedule monitoring service on device boot
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                // Start the schedule monitoring service
                try {
                    ScheduleMonitorService.start(context)
                } catch (e: Exception) {
                    // Handle any exceptions that might occur during service start
                    e.printStackTrace()
                }
            }
        }
    }
}