package com.echo.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echo.app.data.model.EchoEmoji
import com.echo.app.data.model.ECHO_EMOJIS
import com.echo.app.data.repository.EchoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HomeUiState(
    val todayRecord: com.echo.app.data.db.EchoRecord? = null,
    val selectedEmoji: EchoEmoji? = null,
    val recentWeekRecords: List<Pair<String?, String?>> = emptyList(),
    val isSaving: Boolean = false,
    val showYesterdayPrompt: Boolean = false,
    val dateText: String = "",
    val isTodayRecorded: Boolean = false
)

class HomeViewModel(
    private val repository: EchoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDateInfo()
        observeToday()
        observeRecentWeek()
        checkYesterdayPrompt()
    }

    private fun loadDateInfo() {
        val today = LocalDate.now(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINESE)
        _uiState.update { it.copy(dateText = today.format(formatter)) }
    }

    private fun observeToday() {
        viewModelScope.launch {
            repository.observeTodayRecord().collect { record ->
                _uiState.update {
                    it.copy(
                        todayRecord = record,
                        isTodayRecorded = record != null
                    )
                }
            }
        }
    }

    private fun observeRecentWeek() {
        viewModelScope.launch {
            val today = LocalDate.now(ZoneId.systemDefault())
            val weekAgo = today.minusDays(6)
            val startMillis = weekAgo.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            repository.observeRange(startMillis, endMillis).collect { records ->
                val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")
                val dayOfWeek = today.dayOfWeek.value
                val shiftedLabels = (0 until 7).map { i ->
                    val idx = ((dayOfWeek - 1 - (6 - i)) % 7 + 7) % 7
                    dayLabels[idx]
                }

                val weekData = (0 until 7).map { i ->
                    val day = weekAgo.plusDays(i.toLong())
                    val dayMillis = day.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val record = records.find { it.dateMillis == dayMillis }
                    Pair(record?.emoji, shiftedLabels[i])
                }

                _uiState.update { it.copy(recentWeekRecords = weekData) }
            }
        }
    }

    private fun checkYesterdayPrompt() {
        viewModelScope.launch {
            val now = java.time.LocalTime.now(ZoneId.systemDefault())
            if (now.hour == 0 && now.minute <= 15) {
                val todayRecord = repository.getTodayRecord()
                if (todayRecord == null) {
                    _uiState.update { it.copy(showYesterdayPrompt = true) }
                }
            }
        }
    }

    fun selectEmoji(emoji: EchoEmoji) {
        if (!_uiState.value.isTodayRecorded) {
            _uiState.update { it.copy(selectedEmoji = emoji) }
        }
    }

    fun saveRecord() {
        val selected = _uiState.value.selectedEmoji ?: return
        if (_uiState.value.isTodayRecorded) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                repository.saveRecord(selected.emoji, selected.label)
                _uiState.update {
                    it.copy(
                        selectedEmoji = null,
                        isSaving = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun dismissYesterdayPrompt() {
        _uiState.update { it.copy(showYesterdayPrompt = false) }
    }
}
