package com.henrytheaddict.rmssceduale.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.henrytheaddict.rmssceduale.R
import com.henrytheaddict.rmssceduale.data.SchoolPeriod
import com.henrytheaddict.rmssceduale.presentation.MainActivity

/**
 * Service to handle notifications for period alerts
 */
class NotificationService(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "school_schedule_notifications"
        const val NOTIFICATION_ID = 1001
        private const val CHANNEL_NAME = "School Schedule Alerts"
        private const val CHANNEL_DESCRIPTION = "Notifications for upcoming period changes"
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Create notification channel for Android O and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Show notification for period ending soon
     */
    fun showPeriodEndingNotification(
        currentPeriod: SchoolPeriod,
        minutesRemaining: Int,
        enableVibration: Boolean = true,
        enableSound: Boolean = false
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Period Ending Soon")
            .setContentText("${currentPeriod.name} ends in $minutesRemaining minutes")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${currentPeriod.name} will end in $minutesRemaining minutes. Get ready for the next period.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .apply {
                if (enableVibration) {
                    setVibrate(longArrayOf(0, 250, 250, 250))
                }
                if (!enableSound) {
                    setSilent(true)
                }
            }
            .build()
        
        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
            e.printStackTrace()
        }
    }
    
    /**
     * Show notification for next period starting
     */
    fun showNextPeriodNotification(
        nextPeriod: SchoolPeriod,
        minutesUntilStart: Int,
        enableVibration: Boolean = true,
        enableSound: Boolean = false
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Next Period Starting")
            .setContentText("${nextPeriod.name} starts in $minutesUntilStart minutes")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${nextPeriod.name} will start in $minutesUntilStart minutes. Time: ${nextPeriod.getTimeRange()}")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .apply {
                if (enableVibration) {
                    setVibrate(longArrayOf(0, 250, 250, 250))
                }
                if (!enableSound) {
                    setSilent(true)
                }
            }
            .build()
        
        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
            e.printStackTrace()
        }
    }
    
    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
    
    /**
     * Cancel specific notification
     */
    fun cancelNotification(notificationId: Int = NOTIFICATION_ID) {
        notificationManager.cancel(notificationId)
    }
    
    /**
     * Check if notifications are enabled
     */
    fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
}