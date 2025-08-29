package com.henrytheaddict.rmssceduale.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.henrytheaddict.rmssceduale.data.UserPreferences
import com.henrytheaddict.rmssceduale.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the settings screen
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ScheduleRepository(application)
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentPreferences()
    }
    
    private fun loadCurrentPreferences() {
        viewModelScope.launch {
            val prefs = repository.getUserPreferences()
            _uiState.value = _uiState.value.copy(
                notificationsEnabled = prefs.notificationsEnabled,
                notificationMinutesBefore = prefs.notificationMinutesBefore,
                endTimeSeconds = prefs.endTimeSeconds,
                vibrationEnabled = prefs.vibrationEnabled,
                soundEnabled = prefs.soundEnabled,
                showSecondsInCountdown = prefs.showSecondsInCountdown,
                use24HourFormat = prefs.use24HourFormat
            )
        }
    }
    
    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        savePreferences()
    }
    
    fun setNotificationMinutesBefore(minutes: Int) {
        _uiState.value = _uiState.value.copy(notificationMinutesBefore = minutes)
        savePreferences()
    }
    
    fun setEndTimeSeconds(seconds: Int) {
        _uiState.value = _uiState.value.copy(endTimeSeconds = seconds)
        savePreferences()
    }
    
    fun toggleVibration(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(vibrationEnabled = enabled)
        savePreferences()
    }
    
    fun toggleSound(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(soundEnabled = enabled)
        savePreferences()
    }
    
    fun toggleShowSeconds(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(showSecondsInCountdown = enabled)
        savePreferences()
    }
    
    fun toggle24HourFormat(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(use24HourFormat = enabled)
        savePreferences()
    }
    
    private fun savePreferences() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val preferences = UserPreferences(
                notificationsEnabled = currentState.notificationsEnabled,
                notificationMinutesBefore = currentState.notificationMinutesBefore,
                endTimeSeconds = currentState.endTimeSeconds,
                vibrationEnabled = currentState.vibrationEnabled,
                soundEnabled = currentState.soundEnabled,
                showSecondsInCountdown = currentState.showSecondsInCountdown,
                use24HourFormat = currentState.use24HourFormat
            )
            repository.saveUserPreferences(preferences)
        }
    }
    
    fun resetToDefaults() {
        _uiState.value = SettingsUiState() // Reset to default values
        savePreferences()
    }
}

/**
 * UI State for the settings screen
 */
data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val notificationMinutesBefore: Int = 5,
    val endTimeSeconds: Int = 0,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val showSecondsInCountdown: Boolean = true,
    val use24HourFormat: Boolean = false,
    val isLoading: Boolean = false
)