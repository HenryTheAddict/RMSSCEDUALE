package com.henrytheaddict.rmssceduale.data

/**
 * User preferences for the school schedule app
 */
data class UserPreferences(
    val notificationsEnabled: Boolean = true,
    val notificationMinutesBefore: Int = 5,
    val endTimeSeconds: Int = 0,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val showSecondsInCountdown: Boolean = true,
    val use24HourFormat: Boolean = false
) {
    companion object {
        const val PREFS_NAME = "school_schedule_prefs"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_NOTIFICATION_MINUTES_BEFORE = "notification_minutes_before"
        const val KEY_END_TIME_SECONDS = "end_time_seconds"
        const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        const val KEY_SOUND_ENABLED = "sound_enabled"
        const val KEY_SHOW_SECONDS = "show_seconds_in_countdown"
        const val KEY_24_HOUR_FORMAT = "use_24_hour_format"
    }
}