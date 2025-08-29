package com.henrytheaddict.rmssceduale.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Settings",
            style = MaterialTheme.typography.title2,
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        // Notifications Section
        SettingsSection(title = "Notifications") {
            ToggleChip(
                label = {
                    Text(
                        text = "Enable Notifications",
                        style = MaterialTheme.typography.body2
                    )
                },
                checked = uiState.notificationsEnabled,
                onCheckedChange = viewModel::toggleNotifications,
                toggleControl = {
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = null
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            if (uiState.notificationsEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Minutes before notification
                Text(
                    text = "Alert ${uiState.notificationMinutesBefore} min before",
                    style = MaterialTheme.typography.caption1,
                    color = MaterialTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(1, 3, 5, 10).forEach { minutes ->
                        Chip(
                            label = {
                                Text(
                                    text = "${minutes}m",
                                    style = MaterialTheme.typography.caption2
                                )
                            },
                            onClick = { viewModel.setNotificationMinutesBefore(minutes) },
                            colors = ChipDefaults.chipColors(
                                backgroundColor = if (uiState.notificationMinutesBefore == minutes) {
                                    MaterialTheme.colors.primary
                                } else {
                                    MaterialTheme.colors.surface
                                }
                            ),
                            modifier = Modifier.size(width = 40.dp, height = 32.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ToggleChip(
                    label = {
                        Text(
                            text = "Vibration",
                            style = MaterialTheme.typography.body2
                        )
                    },
                    checked = uiState.vibrationEnabled,
                    onCheckedChange = viewModel::toggleVibration,
                    toggleControl = {
                        Switch(
                            checked = uiState.vibrationEnabled,
                            onCheckedChange = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                ToggleChip(
                    label = {
                        Text(
                            text = "Sound",
                            style = MaterialTheme.typography.body2
                        )
                    },
                    checked = uiState.soundEnabled,
                    onCheckedChange = viewModel::toggleSound,
                    toggleControl = {
                        Switch(
                            checked = uiState.soundEnabled,
                            onCheckedChange = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Display Section
        SettingsSection(title = "Display") {
            ToggleChip(
                label = {
                    Text(
                        text = "Show Seconds",
                        style = MaterialTheme.typography.body2
                    )
                },
                checked = uiState.showSecondsInCountdown,
                onCheckedChange = viewModel::toggleShowSeconds,
                toggleControl = {
                    Switch(
                        checked = uiState.showSecondsInCountdown,
                        onCheckedChange = null
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            ToggleChip(
                label = {
                    Text(
                        text = "24-Hour Format",
                        style = MaterialTheme.typography.body2
                    )
                },
                checked = uiState.use24HourFormat,
                onCheckedChange = viewModel::toggle24HourFormat,
                toggleControl = {
                    Switch(
                        checked = uiState.use24HourFormat,
                        onCheckedChange = null
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Timing Section
        SettingsSection(title = "Timing") {
            Text(
                text = "End Time Seconds: ${uiState.endTimeSeconds}",
                style = MaterialTheme.typography.caption1,
                color = MaterialTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(0, 15, 30, 45).forEach { seconds ->
                    Chip(
                        label = {
                            Text(
                                text = if (seconds == 0) "00" else "$seconds",
                                style = MaterialTheme.typography.caption2
                            )
                        },
                        onClick = { viewModel.setEndTimeSeconds(seconds) },
                        colors = ChipDefaults.chipColors(
                            backgroundColor = if (uiState.endTimeSeconds == seconds) {
                                MaterialTheme.colors.primary
                            } else {
                                MaterialTheme.colors.surface
                            }
                        ),
                        modifier = Modifier.size(width = 40.dp, height = 32.dp)
                    )
                }
            }
            
            Text(
                text = "Customize which second of the minute periods end",
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Reset Button
        Chip(
            label = {
                Text(
                    text = "Reset to Defaults",
                    style = MaterialTheme.typography.body2
                )
            },
            onClick = viewModel::resetToDefaults,
            colors = ChipDefaults.chipColors(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.title3,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}