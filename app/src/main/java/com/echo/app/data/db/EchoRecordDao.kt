package com.echo.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EchoRecordDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(record: EchoRecord): Long

    @Query("SELECT * FROM echo_records WHERE dateMillis = :dateMillis LIMIT 1")
    suspend fun getByDate(dateMillis: Long): EchoRecord?

    @Query("SELECT * FROM echo_records WHERE dateMillis = :dateMillis LIMIT 1")
    fun observeByDate(dateMillis: Long): Flow<EchoRecord?>

    @Query("SELECT * FROM echo_records ORDER BY dateMillis DESC")
    fun observeAll(): Flow<List<EchoRecord>>

    @Query("SELECT * FROM echo_records WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis ASC")
    fun observeRange(startMillis: Long, endMillis: Long): Flow<List<EchoRecord>>

    @Query("SELECT * FROM echo_records ORDER BY dateMillis ASC LIMIT 1")
    suspend fun getOldest(): EchoRecord?

    @Query("SELECT COUNT(*) FROM echo_records")
    suspend fun count(): Int

    @Query("DELETE FROM echo_records")
    suspend fun deleteAll()
}
