package com.echo.app.data.repository

import com.echo.app.data.db.EchoRecord
import com.echo.app.data.db.EchoRecordDao
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

open class EchoRepository(private val dao: EchoRecordDao) {

    open fun todayDateMillis(): Long {
        val today = LocalDate.now(ZoneId.systemDefault())
        return today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    open suspend fun getTodayRecord(): EchoRecord? {
        return dao.getByDate(todayDateMillis())
    }

    open fun observeTodayRecord(): Flow<EchoRecord?> {
        return dao.observeByDate(todayDateMillis())
    }

    open suspend fun saveRecord(emoji: String, label: String): Long {
        val now = Instant.now().toEpochMilli()
        val record = EchoRecord(
            emoji = emoji,
            label = label,
            dateMillis = todayDateMillis(),
            createdAtMillis = now
        )
        return dao.insert(record)
    }

    open fun observeAll(): Flow<List<EchoRecord>> {
        return dao.observeAll()
    }

    open fun observeRange(startMillis: Long, endMillis: Long): Flow<List<EchoRecord>> {
        return dao.observeRange(startMillis, endMillis)
    }

    open fun observeMonth(year: Int, month: Int): Flow<List<EchoRecord>> {
        val startOfMonth = LocalDate.of(year, month, 1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = LocalDate.of(year, month, 1)
            .plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        return dao.observeRange(startOfMonth, endOfMonth)
    }

    open suspend fun getOldestRecord(): EchoRecord? = dao.getOldest()

    open suspend fun getTotalCount(): Int = dao.count()

    open suspend fun deleteAll() = dao.deleteAll()
}
