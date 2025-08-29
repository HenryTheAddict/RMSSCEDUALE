package com.henrytheaddict.rmssceduale.repository

import android.content.Context
import com.henrytheaddict.rmssceduale.data.ScheduleConfig
import com.henrytheaddict.rmssceduale.data.SchoolPeriod
import com.henrytheaddict.rmssceduale.service.ScheduleTimeService
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalTime

/**
 * Repository to manage schedule data and provide unified access to schedule information
 */
class ScheduleRepository(context: Context) {
    
    private val timeService = ScheduleTimeService(context)
    private val scheduleConfig = ScheduleConfig.getDefaultSchedule()
    
    // Expose StateFlows from the time service
    val currentPeriod: StateFlow<SchoolPeriod?> = timeService.currentPeriod
    val nextPeriod: StateFlow<SchoolPeriod?> = timeService.nextPeriod
    val remainingTime: StateFlow<Long> = timeService.remainingTime
    val scheduleStatus: StateFlow<ScheduleTimeService.ScheduleStatus> = timeService.scheduleStatus
    
    /**
     * Update current time and refresh all schedule data
     */
    fun updateTime(currentTime: LocalTime = LocalTime.now()) {
        timeService.updateCurrentTime(currentTime)
    }
    
    /**
     * Get all periods in the schedule
     */
    fun getAllPeriods(): List<SchoolPeriod> {
        return scheduleConfig.periods
    }
    
    /**
     * Get formatted remaining time string
     */
    fun getFormattedRemainingTime(): String {
        return timeService.getFormattedRemainingTime()
    }
    
    /**
     * Get current period display text
     */
    fun getCurrentPeriodDisplayText(): String {
        return timeService.getCurrentPeriodDisplayText()
    }
    
    /**
     * Check if notification should be triggered
     */
    fun shouldTriggerNotification(): Boolean {
        return timeService.shouldTriggerNotification()
    }
    
    /**
     * Get user preferences
     */
    fun getUserPreferences() = timeService.getUserPreferences()
    
    /**
     * Save user preferences
     */
    fun saveUserPreferences(preferences: com.henrytheaddict.rmssceduale.data.UserPreferences) {
        timeService.saveUserPreferences(preferences)
    }
    
    /**
     * Get time formatter based on user preference
     */
    fun getTimeFormatter() = timeService.getTimeFormatter()
}