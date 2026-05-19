package com.echo.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "echo_records")
data class EchoRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val emoji: String,
    val label: String,
    val dateMillis: Long,
    val createdAtMillis: Long
)
