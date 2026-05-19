package com.echo.app.ui.trend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.echo.app.data.db.EchoRecord
import com.echo.app.data.model.ECHO_EMOJIS
import com.echo.app.data.repository.EchoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class EmojiDistribution(
    val emoji: String,
    val label: String,
    val count: Int,
    val percentage: Float
)

data class TrendUiState(
    val totalCount: Int = 0,
    val earliestDate: String = "",
    val streak: Int = 0,
    val topKeywords: List<String> = emptyList(),
    val distributions: List<EmojiDistribution> = emptyList(),
    val isLoading: Boolean = true
)

class TrendViewModel(
    private val repository: EchoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrendUiState())
    val uiState: StateFlow<TrendUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            repository.observeAll().collect { records ->
                val stats = calculateStats(records)
                _uiState.update {
                    it.copy(
                        totalCount = stats.totalCount,
                        earliestDate = stats.earliestDate,
                        streak = stats.streak,
                        topKeywords = stats.topKeywords,
                        distributions = stats.distributions,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun calculateStats(records: List<EchoRecord>): TrendStats {
        val totalCount = records.size

        val earliestDate = if (records.isEmpty()) {
            ""
        } else {
            val oldest = records.minByOrNull { it.dateMillis }
            val date = LocalDate.ofInstant(Instant.ofEpochMilli(oldest!!.dateMillis), ZoneId.systemDefault())
            date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.CHINESE))
        }

        val today = LocalDate.now(ZoneId.systemDefault())
        val streak = calculateStreak(records, today)

        val emojiCounts = mutableMapOf<String, Int>()
        records.forEach { record ->
            emojiCounts[record.emoji] = (emojiCounts[record.emoji] ?: 0) + 1
        }

        val emojiLabelMap = ECHO_EMOJIS.associate { it.emoji to it.label }

        val distributions = emojiCounts.entries
            .map { (emoji, count) ->
                EmojiDistribution(
                    emoji = emoji,
                    label = emojiLabelMap[emoji] ?: emoji,
                    count = count,
                    percentage = if (totalCount > 0) count.toFloat() / totalCount else 0f
                )
            }
            .sortedByDescending { it.count }

        val topKeywords = distributions
            .take(3)
            .map { it.label }

        return TrendStats(totalCount, earliestDate, streak, topKeywords, distributions)
    }

    private fun calculateStreak(records: List<EchoRecord>, today: LocalDate): Int {
        val recordDateSet = records.map { r ->
            LocalDate.ofInstant(Instant.ofEpochMilli(r.dateMillis), ZoneId.systemDefault())
        }.toSet()

        var streak = 0
        var checkDate = if (recordDateSet.contains(today)) today else today.minusDays(1)

        while (recordDateSet.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        return streak
    }

    private data class TrendStats(
        val totalCount: Int,
        val earliestDate: String,
        val streak: Int,
        val topKeywords: List<String>,
        val distributions: List<EmojiDistribution>
    )
}
