package com.henrytheaddict.rmssceduale.data

import java.time.LocalTime

/**
 * Configuration class for the school schedule
 */
data class ScheduleConfig(
    val periods: List<SchoolPeriod>,
    val endTimeSeconds: Int = 0, // Configurable seconds for period end times
    val notificationsEnabled: Boolean = true,
    val notificationMinutesBefore: Int = 5
) {
    companion object {
        /**
         * Default school schedule as specified in requirements
         */
        fun getDefaultSchedule(): ScheduleConfig {
            val periods = listOf(
                SchoolPeriod(
                    id = 1,
                    name = "Arrival",
                    startTime = LocalTime.of(8, 0),
                    endTime = LocalTime.of(8, 15)
                ),
                SchoolPeriod(
                    id = 2,
                    name = "Homeroom",
                    startTime = LocalTime.of(8, 15),
                    endTime = LocalTime.of(8, 25)
                ),
                SchoolPeriod(
                    id = 3,
                    name = "Period 1",
                    startTime = LocalTime.of(8, 25),
                    endTime = LocalTime.of(9, 45)
                ),
                SchoolPeriod(
                    id = 4,
                    name = "Period 2",
                    startTime = LocalTime.of(9, 45),
                    endTime = LocalTime.of(11, 5)
                ),
                SchoolPeriod(
                    id = 5,
                    name = "Period 3",
                    startTime = LocalTime.of(11, 5),
                    endTime = LocalTime.of(12, 25)
                ),
                SchoolPeriod(
                    id = 6,
                    name = "Lunch",
                    startTime = LocalTime.of(12, 25),
                    endTime = LocalTime.of(12, 55),
                    isBreak = true
                ),
                SchoolPeriod(
                    id = 7,
                    name = "Advisory",
                    startTime = LocalTime.of(12, 55),
                    endTime = LocalTime.of(13, 35)
                ),
                SchoolPeriod(
                    id = 8,
                    name = "Period 4",
                    startTime = LocalTime.of(13, 35),
                    endTime = LocalTime.of(14, 55)
                ),
                SchoolPeriod(
                    id = 9,
                    name = "Device Drop Off",
                    startTime = LocalTime.of(14, 55),
                    endTime = LocalTime.of(15, 0)
                )
            )
            
            return ScheduleConfig(periods = periods)
        }
    }
    
    /**
     * Find the current active period
     */
    fun getCurrentPeriod(currentTime: LocalTime): SchoolPeriod? {
        return periods.find { it.isCurrentPeriod(currentTime) }
    }
    
    /**
     * Find the next upcoming period
     */
    fun getNextPeriod(currentTime: LocalTime): SchoolPeriod? {
        return periods.find { currentTime.isBefore(it.startTime) }
    }
    
    /**
     * Check if school day has ended
     */
    fun isSchoolDayEnded(currentTime: LocalTime): Boolean {
        val lastPeriod = periods.lastOrNull() ?: return true
        return !currentTime.isBefore(lastPeriod.endTime)
    }
    
    /**
     * Check if school day has started
     */
    fun isSchoolDayStarted(currentTime: LocalTime): Boolean {
        val firstPeriod = periods.firstOrNull() ?: return false
        return !currentTime.isBefore(firstPeriod.startTime)
    }
}