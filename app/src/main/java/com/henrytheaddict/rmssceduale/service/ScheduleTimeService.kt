package com.henrytheaddict.rmssceduale.service

import android.content.Context
import android.content.SharedPreferences
import com.henrytheaddict.rmssceduale.data.ScheduleConfig
import com.henrytheaddict.rmssceduale.data.SchoolPeriod
import com.henrytheaddict.rmssceduale.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Service to manage school schedule timing and countdown functionality
 */
class ScheduleTimeService(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        UserPreferences.PREFS_NAME, 
        Context.MODE_PRIVATE
    )
    
    private val _currentPeriod = MutableStateFlow<SchoolPeriod?>(null)
    val currentPeriod: StateFlow<SchoolPeriod?> = _currentPeriod.asStateFlow()
    
    private val _nextPeriod = MutableStateFlow<SchoolPeriod?>(null)
    val nextPeriod: StateFlow<SchoolPeriod?> = _nextPeriod.asStateFlow()
    
    private val _remainingTime = MutableStateFlow<Long>(0L)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()
    
    private val _scheduleStatus = MutableStateFlow(ScheduleStatus.BEFORE_SCHOOL)
    val scheduleStatus: StateFlow<ScheduleStatus> = _scheduleStatus.asStateFlow()
    
    private var scheduleConfig = ScheduleConfig.getDefaultSchedule()
    
    enum class ScheduleStatus {
        BEFORE_SCHOOL,
        IN_PERIOD,
        BETWEEN_PERIODS,
        AFTER_SCHOOL
    }
    
    /**
     * Update the current time and recalculate period information
     */
    fun updateCurrentTime(currentTime: LocalTime = LocalTime.now()) {
        val adjustedTime = adjustTimeWithCustomSeconds(currentTime)
        
        val current = scheduleConfig.getCurrentPeriod(adjustedTime)
        val next = scheduleConfig.getNextPeriod(adjustedTime)
        
        _currentPeriod.value = current
        _nextPeriod.value = next
        
        // Calculate remaining time
        val remaining = when {
            current != null -> current.getRemainingSeconds(adjustedTime)
            next != null -> {
                val currentSeconds = adjustedTime.toSecondOfDay()
                val nextStartSeconds = next.startTime.toSecondOfDay()
                (nextStartSeconds - currentSeconds).toLong()
            }
            else -> 0L
        }
        
        _remainingTime.value = remaining
        
        // Update schedule status
        _scheduleStatus.value = when {
            !scheduleConfig.isSchoolDayStarted(adjustedTime) -> ScheduleStatus.BEFORE_SCHOOL
            scheduleConfig.isSchoolDayEnded(adjustedTime) -> ScheduleStatus.AFTER_SCHOOL
            current != null -> ScheduleStatus.IN_PERIOD
            else -> ScheduleStatus.BETWEEN_PERIODS
        }
    }
    
    /**
     * Adjust time with custom end-time seconds setting
     */
    private fun adjustTimeWithCustomSeconds(time: LocalTime): LocalTime {
        val endTimeSeconds = getUserPreferences().endTimeSeconds
        return if (endTimeSeconds != 0) {
            time.withSecond(endTimeSeconds)
        } else {
            time
        }
    }
    
    /**
     * Get formatted remaining time string
     */
    fun getFormattedRemainingTime(): String {
        val seconds = _remainingTime.value
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        
        return if (getUserPreferences().showSecondsInCountdown) {
            String.format("%02d:%02d", minutes, remainingSeconds)
        } else {
            String.format("%d min", minutes)
        }
    }
    
    /**
     * Get current period display text
     */
    fun getCurrentPeriodDisplayText(): String {
        return when (_scheduleStatus.value) {
            ScheduleStatus.BEFORE_SCHOOL -> "School starts soon"
            ScheduleStatus.AFTER_SCHOOL -> "School day ended"
            ScheduleStatus.IN_PERIOD -> _currentPeriod.value?.name ?: "Unknown Period"
            ScheduleStatus.BETWEEN_PERIODS -> {
                val next = _nextPeriod.value
                if (next != null) {
                    "Next: ${next.name}"
                } else {
                    "Break time"
                }
            }
        }
    }
    
    /**
     * Check if notification should be triggered
     */
    fun shouldTriggerNotification(): Boolean {
        val prefs = getUserPreferences()
        if (!prefs.notificationsEnabled) return false
        
        val remainingMinutes = _remainingTime.value / 60
        return remainingMinutes == prefs.notificationMinutesBefore.toLong() && 
               _scheduleStatus.value == ScheduleStatus.IN_PERIOD
    }
    
    /**
     * Get user preferences from SharedPreferences
     */
    fun getUserPreferences(): UserPreferences {
        return UserPreferences(
            notificationsEnabled = prefs.getBoolean(UserPreferences.KEY_NOTIFICATIONS_ENABLED, true),
            notificationMinutesBefore = prefs.getInt(UserPreferences.KEY_NOTIFICATION_MINUTES_BEFORE, 5),
            endTimeSeconds = prefs.getInt(UserPreferences.KEY_END_TIME_SECONDS, 0),
            vibrationEnabled = prefs.getBoolean(UserPreferences.KEY_VIBRATION_ENABLED, true),
            soundEnabled = prefs.getBoolean(UserPreferences.KEY_SOUND_ENABLED, false),
            showSecondsInCountdown = prefs.getBoolean(UserPreferences.KEY_SHOW_SECONDS, true),
            use24HourFormat = prefs.getBoolean(UserPreferences.KEY_24_HOUR_FORMAT, false)
        )
    }
    
    /**
     * Save user preferences to SharedPreferences
     */
    fun saveUserPreferences(preferences: UserPreferences) {
        prefs.edit().apply {
            putBoolean(UserPreferences.KEY_NOTIFICATIONS_ENABLED, preferences.notificationsEnabled)
            putInt(UserPreferences.KEY_NOTIFICATION_MINUTES_BEFORE, preferences.notificationMinutesBefore)
            putInt(UserPreferences.KEY_END_TIME_SECONDS, preferences.endTimeSeconds)
            putBoolean(UserPreferences.KEY_VIBRATION_ENABLED, preferences.vibrationEnabled)
            putBoolean(UserPreferences.KEY_SOUND_ENABLED, preferences.soundEnabled)
            putBoolean(UserPreferences.KEY_SHOW_SECONDS, preferences.showSecondsInCountdown)
            putBoolean(UserPreferences.KEY_24_HOUR_FORMAT, preferences.use24HourFormat)
            apply()
        }
    }
    
    /**
     * Get time format based on user preference
     */
    fun getTimeFormatter(): DateTimeFormatter {
        return if (getUserPreferences().use24HourFormat) {
            DateTimeFormatter.ofPattern("HH:mm")
        } else {
            DateTimeFormatter.ofPattern("h:mm a")
        }
    }
}