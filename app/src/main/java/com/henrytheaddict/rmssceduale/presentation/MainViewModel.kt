package com.henrytheaddict.rmssceduale.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.henrytheaddict.rmssceduale.data.SchoolPeriod
import com.henrytheaddict.rmssceduale.repository.ScheduleRepository
import com.henrytheaddict.rmssceduale.service.ScheduleTimeService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalTime

/**
 * ViewModel for the main schedule display screen
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ScheduleRepository(application)
    
    // UI State
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private val _isAmbientMode = MutableStateFlow(false)
    val isAmbientMode: StateFlow<Boolean> = _isAmbientMode.asStateFlow()
    
    init {
        startTimeUpdates()
        observeScheduleChanges()
    }
    
    /**
     * Start periodic time updates
     */
    private fun startTimeUpdates() {
        viewModelScope.launch {
            while (true) {
                repository.updateTime()
                delay(1000) // Update every second
            }
        }
    }
    
    /**
     * Observe schedule changes and update UI state
     */
    private fun observeScheduleChanges() {
        viewModelScope.launch {
            combine(
                repository.currentPeriod,
                repository.nextPeriod,
                repository.remainingTime,
                repository.scheduleStatus
            ) { currentPeriod, nextPeriod, remainingTime, status ->
                _uiState.value = _uiState.value.copy(
                    currentPeriod = currentPeriod,
                    nextPeriod = nextPeriod,
                    remainingTimeSeconds = remainingTime,
                    formattedRemainingTime = repository.getFormattedRemainingTime(),
                    currentPeriodDisplayText = repository.getCurrentPeriodDisplayText(),
                    scheduleStatus = status,
                    currentTime = LocalTime.now().format(repository.getTimeFormatter())
                )
                
                // Check for notifications
                if (repository.shouldTriggerNotification()) {
                    _uiState.value = _uiState.value.copy(
                        shouldShowNotification = true
                    )
                }
            }.collect { }
        }
    }
    
    /**
     * Set ambient mode state
     */
    fun setAmbientMode(isAmbient: Boolean) {
        _isAmbientMode.value = isAmbient
    }
    
    /**
     * Dismiss notification
     */
    fun dismissNotification() {
        _uiState.value = _uiState.value.copy(shouldShowNotification = false)
    }
    
    /**
     * Get all periods for display
     */
    fun getAllPeriods(): List<SchoolPeriod> {
        return repository.getAllPeriods()
    }
    
    /**
     * Navigate to settings
     */
    fun navigateToSettings() {
        _uiState.value = _uiState.value.copy(showSettings = true)
    }
    
    /**
     * Close settings
     */
    fun closeSettings() {
        _uiState.value = _uiState.value.copy(showSettings = false)
    }
}

/**
 * UI State for the main screen
 */
data class MainUiState(
    val currentPeriod: SchoolPeriod? = null,
    val nextPeriod: SchoolPeriod? = null,
    val remainingTimeSeconds: Long = 0L,
    val formattedRemainingTime: String = "--:--",
    val currentPeriodDisplayText: String = "Loading...",
    val scheduleStatus: ScheduleTimeService.ScheduleStatus = ScheduleTimeService.ScheduleStatus.BEFORE_SCHOOL,
    val currentTime: String = "",
    val shouldShowNotification: Boolean = false,
    val showSettings: Boolean = false,
    val isLoading: Boolean = false
)