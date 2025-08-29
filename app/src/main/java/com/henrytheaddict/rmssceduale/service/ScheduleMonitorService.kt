package com.henrytheaddict.rmssceduale.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.henrytheaddict.rmssceduale.R
import kotlinx.coroutines.*
import java.time.LocalTime

/**
 * Background service to monitor schedule and trigger notifications
 */
class ScheduleMonitorService : Service() {
    
    private lateinit var scheduleTimeService: ScheduleTimeService
    private lateinit var notificationService: NotificationService
    private var monitoringJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private var lastNotificationMinute = -1 // Track to avoid duplicate notifications
    
    override fun onCreate() {
        super.onCreate()
        scheduleTimeService = ScheduleTimeService(this)
        notificationService = NotificationService(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createForegroundNotification())
        startMonitoring()
        return START_STICKY // Restart if killed
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                try {
                    checkForNotifications()
                    delay(30000) // Check every 30 seconds
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(60000) // Wait longer if there's an error
                }
            }
        }
    }
    
    private fun checkForNotifications() {
        val currentTime = LocalTime.now()
        scheduleTimeService.updateCurrentTime(currentTime)
        
        val userPrefs = scheduleTimeService.getUserPreferences()
        
        // Only proceed if notifications are enabled
        if (!userPrefs.notificationsEnabled || !notificationService.areNotificationsEnabled()) {
            return
        }
        
        val currentPeriod = scheduleTimeService.currentPeriod.value
        val remainingSeconds = scheduleTimeService.remainingTime.value
        val scheduleStatus = scheduleTimeService.scheduleStatus.value
        
        // Check for period ending notification
        if (currentPeriod != null && 
            scheduleStatus == ScheduleTimeService.ScheduleStatus.IN_PERIOD) {
            
            val remainingMinutes = (remainingSeconds / 60).toInt()
            
            // Trigger notification when specified minutes remain
            if (remainingMinutes == userPrefs.notificationMinutesBefore && 
                remainingMinutes != lastNotificationMinute) {
                
                notificationService.showPeriodEndingNotification(
                    currentPeriod = currentPeriod,
                    minutesRemaining = remainingMinutes,
                    enableVibration = userPrefs.vibrationEnabled,
                    enableSound = userPrefs.soundEnabled
                )
                
                lastNotificationMinute = remainingMinutes
            }
        }
        
        // Reset notification tracking when period changes
        if (currentPeriod == null || remainingSeconds <= 0) {
            lastNotificationMinute = -1
        }
        
        // Optional: Notify about next period starting (for breaks)
        val nextPeriod = scheduleTimeService.nextPeriod.value
        if (scheduleStatus == ScheduleTimeService.ScheduleStatus.BETWEEN_PERIODS && 
            nextPeriod != null && remainingSeconds > 0) {
            
            val minutesUntilNext = (remainingSeconds / 60).toInt()
            
            // Notify 2 minutes before next period starts
            if (minutesUntilNext == 2 && minutesUntilNext != lastNotificationMinute) {
                notificationService.showNextPeriodNotification(
                    nextPeriod = nextPeriod,
                    minutesUntilStart = minutesUntilNext,
                    enableVibration = userPrefs.vibrationEnabled,
                    enableSound = userPrefs.soundEnabled
                )
                
                lastNotificationMinute = minutesUntilNext
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        monitoringJob?.cancel()
        serviceScope.cancel()
    }
    
    /**
     * Create notification channel for the foreground service
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background service for monitoring school schedule"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Create foreground service notification
     */
    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("School Schedule Monitor")
            .setContentText("Monitoring schedule for notifications")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
    
    companion object {
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "schedule_monitor_service"
        private const val CHANNEL_NAME = "Schedule Monitor"
        
        /**
         * Start the monitoring service
         */
        fun start(context: android.content.Context) {
            val intent = Intent(context, ScheduleMonitorService::class.java)
            context.startForegroundService(intent)
        }
        
        /**
         * Stop the monitoring service
         */
        fun stop(context: android.content.Context) {
            val intent = Intent(context, ScheduleMonitorService::class.java)
            context.stopService(intent)
        }
    }
}