package com.henrytheaddict.rmssceduale.presentation

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.henrytheaddict.rmssceduale.presentation.theme.RMSSCEDUALETheme
import com.henrytheaddict.rmssceduale.service.ScheduleTimeService
import com.henrytheaddict.rmssceduale.service.ScheduleMonitorService

class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider {
    
    private val viewModel: MainViewModel by viewModels()
    private lateinit var ambientController: AmbientModeSupport.AmbientController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        ambientController = AmbientModeSupport.attach(this)
        setTheme(android.R.style.Theme_DeviceDefault)
        
        // Start the background monitoring service
        try {
            ScheduleMonitorService.start(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        setContent {
            ScheduleApp(viewModel = viewModel)
        }
    }
    
    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback {
        return object : AmbientModeSupport.AmbientCallback() {
            override fun onEnterAmbient(ambientDetails: Bundle?) {
                super.onEnterAmbient(ambientDetails)
                viewModel.setAmbientMode(true)
            }
            
            override fun onExitAmbient() {
                super.onExitAmbient()
                viewModel.setAmbientMode(false)
            }
        }
    }
}

@Composable
fun ScheduleApp(viewModel: MainViewModel) {
    val navController = rememberSwipeDismissableNavController()
    val uiState by viewModel.uiState.collectAsState()
    val isAmbient by viewModel.isAmbientMode.collectAsState()
    
    RMSSCEDUALETheme {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "main"
        ) {
            composable("main") {
                MainScreen(
                    uiState = uiState,
                    isAmbient = isAmbient,
                    onSettingsClick = { navController.navigate("settings") },
                    onNotificationDismiss = viewModel::dismissNotification
                )
            }
            composable("settings") {
                com.henrytheaddict.rmssceduale.presentation.SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    uiState: MainUiState,
    isAmbient: Boolean,
    onSettingsClick: () -> Unit,
    onNotificationDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isAmbient) Color.Black else MaterialTheme.colors.background
            )
    ) {
        // Time text at the top
        TimeText(
            modifier = Modifier.fillMaxWidth(),
            timeTextStyle = TimeTextDefaults.timeTextStyle(
                color = if (isAmbient) Color.White else MaterialTheme.colors.onBackground
            )
        )
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Current period name
            Text(
                text = uiState.currentPeriodDisplayText,
                style = MaterialTheme.typography.title2,
                color = if (isAmbient) Color.White else MaterialTheme.colors.primary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Countdown timer
            if (uiState.remainingTimeSeconds > 0) {
                Text(
                    text = uiState.formattedRemainingTime,
                    style = MaterialTheme.typography.display1.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isAmbient) Color.White else getCountdownColor(uiState.remainingTimeSeconds),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                
                Text(
                    text = "remaining",
                    style = MaterialTheme.typography.caption1,
                    color = if (isAmbient) Color.Gray else MaterialTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            // Period time range
            uiState.currentPeriod?.let { period ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = period.getTimeRange(),
                    style = MaterialTheme.typography.body2,
                    color = if (isAmbient) Color.Gray else MaterialTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            // Next period info
            if (uiState.scheduleStatus == ScheduleTimeService.ScheduleStatus.BETWEEN_PERIODS) {
                uiState.nextPeriod?.let { nextPeriod ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Next: ${nextPeriod.name}",
                        style = MaterialTheme.typography.caption1,
                        color = if (isAmbient) Color.Gray else MaterialTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = nextPeriod.getTimeRange(),
                        style = MaterialTheme.typography.caption2,
                        color = if (isAmbient) Color.Gray else MaterialTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Settings button (only in interactive mode)
        if (!isAmbient) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.surface)
                    .clickable { onSettingsClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚙",
                    fontSize = 18.sp,
                    color = MaterialTheme.colors.onSurface
                )
            }
        }
        
        // Notification overlay
        if (uiState.shouldShowNotification && !isAmbient) {
            NotificationOverlay(
                onDismiss = onNotificationDismiss
            )
        }
    }
}

@Composable
fun NotificationOverlay(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            onClick = { onDismiss() }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⏰",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "5 minutes remaining",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "in current period",
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// SettingsScreen is now implemented in SettingsScreen.kt

@Composable
fun getCountdownColor(remainingSeconds: Long): Color {
    val remainingMinutes = remainingSeconds / 60
    return when {
        remainingMinutes <= 2 -> Color.Red
        remainingMinutes <= 5 -> Color(0xFFFF9800) // Orange
        else -> MaterialTheme.colors.primary
    }
}