package com.henrytheaddict.rmssceduale.tile

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.Row
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.Colors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.henrytheaddict.rmssceduale.presentation.MainActivity
import com.henrytheaddict.rmssceduale.repository.ScheduleRepository
import com.henrytheaddict.rmssceduale.service.ScheduleTimeService

private const val RESOURCES_VERSION = "1"

/**
 * Tile service that displays school schedule information
 */
@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    private lateinit var repository: ScheduleRepository

    override fun onCreate() {
        super.onCreate()
        repository = ScheduleRepository(this)
    }

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ) = resources(requestParams)

    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ) = tile(requestParams, this)
}

private fun resources(
    requestParams: RequestBuilders.ResourcesRequest
): ResourceBuilders.Resources {
    return ResourceBuilders.Resources.Builder()
        .setVersion(RESOURCES_VERSION)
        .build()
}

private fun tile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
): TileBuilders.Tile {
    val repository = ScheduleRepository(context)
    repository.updateTime()
    
    val singleTileTimeline = TimelineBuilders.Timeline.Builder()
        .addTimelineEntry(
            TimelineBuilders.TimelineEntry.Builder()
                .setLayout(
                    LayoutElementBuilders.Layout.Builder()
                        .setRoot(tileLayout(requestParams, context, repository))
                        .build()
                )
                .build()
        )
        .build()

    return TileBuilders.Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setTileTimeline(singleTileTimeline)
        .setFreshnessIntervalMillis(30000) // Update every 30 seconds
        .build()
}

private fun tileLayout(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
    repository: ScheduleRepository
): LayoutElementBuilders.LayoutElement {
    val currentPeriod = repository.currentPeriod.value
    val scheduleStatus = repository.scheduleStatus.value
    val remainingTime = repository.getFormattedRemainingTime()
    val currentPeriodText = repository.getCurrentPeriodDisplayText()
    
    // Create click action to open main app
    val clickAction = ActionBuilders.LaunchAction.Builder()
        .setAndroidActivity(
            ActionBuilders.AndroidActivity.Builder()
                .setClassName(MainActivity::class.java.name)
                .setPackageName(context.packageName)
                .build()
        )
        .build()
    
    val clickModifier = ModifiersBuilders.Modifiers.Builder()
        .setClickable(
            ModifiersBuilders.Clickable.Builder()
                .setOnClick(clickAction)
                .build()
        )
        .build()
    
    return PrimaryLayout.Builder(requestParams.deviceConfiguration)
        .setResponsiveContentInsetEnabled(true)
        .setContent(
            createTileContent(context, currentPeriodText, remainingTime, scheduleStatus, clickModifier)
        )
        .build()
}

private fun createTileContent(
    context: Context,
    currentPeriodText: String,
    remainingTime: String,
    scheduleStatus: ScheduleTimeService.ScheduleStatus,
    clickModifier: ModifiersBuilders.Modifiers
): LayoutElementBuilders.LayoutElement {
    return Column.Builder()
        .setModifiers(clickModifier)
        .addContent(
            // Period name
            Text.Builder(context, currentPeriodText)
                .setColor(argb(Colors.DEFAULT.primary))
                .setTypography(Typography.TYPOGRAPHY_TITLE3)
                .setMaxLines(2)
                .build()
        )
        .addContent(
            // Spacing
            LayoutElementBuilders.Spacer.Builder()
                .setHeight(dp(4f))
                .build()
        )
        .addContent(
            // Remaining time or status
            if (scheduleStatus == ScheduleTimeService.ScheduleStatus.IN_PERIOD) {
                Column.Builder()
                    .addContent(
                        Text.Builder(context, remainingTime)
                            .setColor(argb(getTimeColor(remainingTime)))
                            .setTypography(Typography.TYPOGRAPHY_DISPLAY3)
                            .build()
                    )
                    .addContent(
                        Text.Builder(context, "remaining")
                            .setColor(argb(Colors.DEFAULT.onSurfaceVariant))
                            .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                            .build()
                    )
                    .build()
            } else {
                Text.Builder(context, getStatusText(scheduleStatus))
                    .setColor(argb(Colors.DEFAULT.onSurfaceVariant))
                    .setTypography(Typography.TYPOGRAPHY_BODY2)
                    .setMaxLines(2)
                    .build()
            }
        )
        .build()
}

private fun getTimeColor(remainingTime: String): Int {
    // Extract minutes from time string (format: "MM:SS" or "M min")
    val minutes = try {
        if (remainingTime.contains(":")) {
            remainingTime.split(":")[0].toInt()
        } else {
            remainingTime.replace(" min", "").toInt()
        }
    } catch (e: Exception) {
        10 // Default to safe color
    }
    
    return when {
        minutes <= 2 -> 0xFFFF5252.toInt() // Red
        minutes <= 5 -> 0xFFFF9800.toInt() // Orange
        else -> Colors.DEFAULT.primary
    }
}

private fun getStatusText(status: ScheduleTimeService.ScheduleStatus): String {
    return when (status) {
        ScheduleTimeService.ScheduleStatus.BEFORE_SCHOOL -> "School starts soon"
        ScheduleTimeService.ScheduleStatus.AFTER_SCHOOL -> "School day ended"
        ScheduleTimeService.ScheduleStatus.BETWEEN_PERIODS -> "Break time"
        else -> "Loading..."
    }
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun tilePreview(context: Context) = TilePreviewData(::resources) {
    tile(it, context)
}