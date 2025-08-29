package com.henrytheaddict.rmssceduale.data

import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Represents a single school period with start and end times
 */
data class SchoolPeriod(
    val id: Int,
    val name: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isBreak: Boolean = false
) {
    /**
     * Get formatted time range string
     */
    fun getTimeRange(): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        return "${startTime.format(formatter)} - ${endTime.format(formatter)}"
    }
    
    /**
     * Check if current time falls within this period
     */
    fun isCurrentPeriod(currentTime: LocalTime): Boolean {
        return !currentTime.isBefore(startTime) && currentTime.isBefore(endTime)
    }
    
    /**
     * Get remaining time in seconds for this period
     */
    fun getRemainingSeconds(currentTime: LocalTime): Long {
        if (!isCurrentPeriod(currentTime)) return 0L
        
        val currentSeconds = currentTime.toSecondOfDay()
        val endSeconds = endTime.toSecondOfDay()
        return (endSeconds - currentSeconds).toLong()
    }
    
    /**
     * Get duration of this period in minutes
     */
    fun getDurationMinutes(): Long {
        return java.time.Duration.between(startTime, endTime).toMinutes()
    }
}