package com.henrytheaddict.rmssceduale.complication

import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.henrytheaddict.rmssceduale.repository.ScheduleRepository
import com.henrytheaddict.rmssceduale.service.ScheduleTimeService
import java.time.LocalTime

/**
 * Complication data source that provides school schedule information
 */
class MainComplicationService : SuspendingComplicationDataSourceService() {

    private lateinit var repository: ScheduleRepository

    override fun onCreate() {
        super.onCreate()
        repository = ScheduleRepository(this)
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> createShortTextComplication(
                "Period 1", 
                "Current: Period 1"
            )
            ComplicationType.LONG_TEXT -> createLongTextComplication(
                "Period 1 - 15:30 remaining",
                "Current period: Period 1, Time remaining: 15 minutes 30 seconds"
            )
            ComplicationType.RANGED_VALUE -> createRangedValueComplication(
                75f, // 75% of period completed
                "Period 1",
                "Period 1 - 75% complete"
            )
            else -> null
        }
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        // Update repository with current time
        repository.updateTime()
        
        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> createShortTextData()
            ComplicationType.LONG_TEXT -> createLongTextData()
            ComplicationType.RANGED_VALUE -> createRangedValueData()
            else -> createShortTextData() // Fallback
        }
    }

    private fun createShortTextData(): ComplicationData {
        val currentPeriod = repository.currentPeriod.value
        val scheduleStatus = repository.scheduleStatus.value
        
        val (text, description) = when (scheduleStatus) {
            ScheduleTimeService.ScheduleStatus.BEFORE_SCHOOL -> {
                "School" to "School starts soon"
            }
            ScheduleTimeService.ScheduleStatus.AFTER_SCHOOL -> {
                "Done" to "School day ended"
            }
            ScheduleTimeService.ScheduleStatus.IN_PERIOD -> {
                val periodName = currentPeriod?.name ?: "Period"
                val shortName = when {
                    periodName.startsWith("Period") -> periodName.replace("Period ", "P")
                    periodName == "Homeroom" -> "HR"
                    periodName == "Advisory" -> "ADV"
                    periodName == "Device Drop Off" -> "Drop"
                    else -> periodName.take(6)
                }
                shortName to "Current period: $periodName"
            }
            ScheduleTimeService.ScheduleStatus.BETWEEN_PERIODS -> {
                val nextPeriod = repository.nextPeriod.value
                if (nextPeriod != null) {
                    "Next" to "Next: ${nextPeriod.name}"
                } else {
                    "Break" to "Break time"
                }
            }
        }
        
        return createShortTextComplication(text, description)
    }

    private fun createLongTextData(): ComplicationData {
        val currentPeriod = repository.currentPeriod.value
        val remainingTime = repository.getFormattedRemainingTime()
        val scheduleStatus = repository.scheduleStatus.value
        
        val (text, description) = when (scheduleStatus) {
            ScheduleTimeService.ScheduleStatus.BEFORE_SCHOOL -> {
                "School starts soon" to "School day has not started yet"
            }
            ScheduleTimeService.ScheduleStatus.AFTER_SCHOOL -> {
                "School day ended" to "All periods completed for today"
            }
            ScheduleTimeService.ScheduleStatus.IN_PERIOD -> {
                val periodName = currentPeriod?.name ?: "Current Period"
                "$periodName - $remainingTime" to "Current period: $periodName, Time remaining: $remainingTime"
            }
            ScheduleTimeService.ScheduleStatus.BETWEEN_PERIODS -> {
                val nextPeriod = repository.nextPeriod.value
                if (nextPeriod != null) {
                    "Next: ${nextPeriod.name} in $remainingTime" to "Next period: ${nextPeriod.name} starts in $remainingTime"
                } else {
                    "Break time" to "Between periods"
                }
            }
        }
        
        return createLongTextComplication(text, description)
    }

    private fun createRangedValueData(): ComplicationData {
        val currentPeriod = repository.currentPeriod.value
        val remainingSeconds = repository.remainingTime.value
        
        if (currentPeriod != null && remainingSeconds > 0) {
            val totalDuration = currentPeriod.getDurationMinutes() * 60
            val elapsed = totalDuration - remainingSeconds
            val progress = (elapsed.toFloat() / totalDuration.toFloat() * 100f).coerceIn(0f, 100f)
            
            return createRangedValueComplication(
                progress,
                currentPeriod.name,
                "${currentPeriod.name} - ${String.format("%.0f", progress)}% complete"
            )
        } else {
            return createRangedValueComplication(
                0f,
                "Schedule",
                "No active period"
            )
        }
    }

    private fun createShortTextComplication(text: String, contentDescription: String) =
        ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text).build(),
            contentDescription = PlainComplicationText.Builder(contentDescription).build()
        ).build()

    private fun createLongTextComplication(text: String, contentDescription: String) =
        LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text).build(),
            contentDescription = PlainComplicationText.Builder(contentDescription).build()
        ).build()

    private fun createRangedValueComplication(
        value: Float,
        text: String,
        contentDescription: String
    ) = RangedValueComplicationData.Builder(
        value = value,
        min = 0f,
        max = 100f,
        contentDescription = PlainComplicationText.Builder(contentDescription).build()
    ).setText(PlainComplicationText.Builder(text).build())
        .build()
}