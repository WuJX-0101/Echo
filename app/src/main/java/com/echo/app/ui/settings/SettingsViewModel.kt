package com.echo.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echo.app.data.repository.EchoRepository
import com.echo.app.data.repository.SettingsPreferences
import com.echo.app.service.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val notificationEnabled: Boolean = false,
    val notificationHour: Int = 21,
    val darkMode: Boolean = false,
    val showTimePicker: Boolean = false,
    val showClearConfirm: Boolean = false,
    val appVersion: String = "1.0.0"
)

class SettingsViewModel(
    private val app: Context,
    private val settings: SettingsPreferences,
    private val repository: EchoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        NotificationHelper.createChannel(app)
        viewModelScope.launch {
            combine(
                settings.notificationEnabled,
                settings.notificationHour,
                settings.darkMode
            ) { enabled, hour, dark ->
                SettingsUiState(
                    notificationEnabled = enabled,
                    notificationHour = hour,
                    darkMode = dark
                )
            }.collect { state ->
                _uiState.update {
                    it.copy(
                        notificationEnabled = state.notificationEnabled,
                        notificationHour = state.notificationHour,
                        darkMode = state.darkMode
                    )
                }
            }
        }
    }

    fun toggleNotification() {
        viewModelScope.launch {
            val newValue = !_uiState.value.notificationEnabled
            settings.setNotificationEnabled(newValue)
            val prefs = app.getSharedPreferences("echo_settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("notification_enabled", newValue).apply()
            if (newValue) {
                NotificationHelper.schedule(app, _uiState.value.notificationHour)
            } else {
                NotificationHelper.cancel(app)
            }
        }
    }

    fun showTimePicker() {
        _uiState.update { it.copy(showTimePicker = true) }
    }

    fun setNotificationHour(hour: Int) {
        viewModelScope.launch {
            settings.setNotificationHour(hour)
            val prefs = app.getSharedPreferences("echo_settings", Context.MODE_PRIVATE)
            prefs.edit().putInt("notification_hour", hour).apply()
            if (_uiState.value.notificationEnabled) {
                NotificationHelper.schedule(app, hour)
            }
            _uiState.update { it.copy(showTimePicker = false) }
        }
    }

    fun hideTimePicker() {
        _uiState.update { it.copy(showTimePicker = false) }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val newValue = !_uiState.value.darkMode
            settings.setDarkMode(newValue)
        }
    }

    fun showClearConfirm() {
        _uiState.update { it.copy(showClearConfirm = true) }
    }

    fun hideClearConfirm() {
        _uiState.update { it.copy(showClearConfirm = false) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAll()
            _uiState.update { it.copy(showClearConfirm = false) }
        }
    }
}
