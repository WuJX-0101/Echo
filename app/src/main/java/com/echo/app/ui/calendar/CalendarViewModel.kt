package com.echo.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echo.app.data.db.EchoRecord
import com.echo.app.data.repository.EchoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class CalendarDay(
    val date: LocalDate,
    val dayOfMonth: Int,
    val emoji: String? = null,
    val label: String? = null,
    val isToday: Boolean = false,
    val isCurrentMonth: Boolean = true,
    val hasRecord: Boolean = false
)

data class CalendarUiState(
    val year: Int = 0,
    val month: Int = 0,
    val monthLabel: String = "",
    val days: List<CalendarDay> = emptyList(),
    val totalRecorded: Int = 0,
    val totalDays: Int = 0,
    val selectedDay: CalendarDay? = null,
    val isLoading: Boolean = true
)

class CalendarViewModel(
    private val repository: EchoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        val today = LocalDate.now(ZoneId.systemDefault())
        navigateTo(today.year, today.monthValue)
    }

    fun navigateTo(year: Int, month: Int) {
        val label = YearMonth.of(year, month)
            .getMonth()
            .getDisplayName(TextStyle.FULL, Locale.CHINESE)
        _uiState.update {
            it.copy(
                year = year,
                month = month,
                monthLabel = "${year}年${label}",
                isLoading = true
            )
        }
        loadMonth(year, month)
    }

    private fun loadMonth(year: Int, month: Int) {
        viewModelScope.launch {
            repository.observeMonth(year, month).collect { records ->
                val today = LocalDate.now(ZoneId.systemDefault())
                val days = buildCalendarGrid(year, month, records, today)
                val totalDays = YearMonth.of(year, month).lengthOfMonth()
                _uiState.update {
                    it.copy(
                        days = days,
                        totalRecorded = records.size,
                        totalDays = totalDays,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun buildCalendarGrid(
        year: Int,
        month: Int,
        records: List<EchoRecord>,
        today: LocalDate
    ): List<CalendarDay> {
        val yearMonth = YearMonth.of(year, month)
        val firstOfMonth = yearMonth.atDay(1)
        val lastOfMonth = yearMonth.atEndOfMonth()
        val startDow = firstOfMonth.dayOfWeek.value  // 1=Mon ... 7=Sun

        val prevMonth = yearMonth.minusMonths(1)
        val prevMonthDays = prevMonth.lengthOfMonth()

        val days = mutableListOf<CalendarDay>()

        // Previous month padding
        val paddingStart = startDow - 1
        for (i in 0 until paddingStart) {
            val day = prevMonthDays - paddingStart + 1 + i
            val date = prevMonth.atDay(day)
            days.add(CalendarDay(date = date, dayOfMonth = day, isCurrentMonth = false))
        }

        // Current month
        for (day in 1..yearMonth.lengthOfMonth()) {
            val date = yearMonth.atDay(day)
            val record = records.find { r ->
                val rDate = LocalDate.ofInstant(Instant.ofEpochMilli(r.dateMillis), ZoneId.systemDefault())
                rDate == date
            }
            days.add(
                CalendarDay(
                    date = date,
                    dayOfMonth = day,
                    emoji = record?.emoji,
                    label = record?.label,
                    isToday = date == today,
                    isCurrentMonth = true,
                    hasRecord = record != null
                )
            )
        }

        // Next month padding to fill 42 cells (6 rows × 7 cols)
        val remaining = 42 - days.size
        for (day in 1..remaining) {
            val date = yearMonth.plusMonths(1).atDay(day)
            days.add(CalendarDay(date = date, dayOfMonth = day, isCurrentMonth = false))
        }

        return days
    }

    fun previousMonth() {
        val state = _uiState.value
        val ym = YearMonth.of(state.year, state.month).minusMonths(1)
        navigateTo(ym.year, ym.monthValue)
    }

    fun nextMonth() {
        val state = _uiState.value
        val ym = YearMonth.of(state.year, state.month).plusMonths(1)
        navigateTo(ym.year, ym.monthValue)
    }

    fun selectDay(day: CalendarDay) {
        _uiState.update { it.copy(selectedDay = day) }
    }

    fun dismissDetail() {
        _uiState.update { it.copy(selectedDay = null) }
    }
}
