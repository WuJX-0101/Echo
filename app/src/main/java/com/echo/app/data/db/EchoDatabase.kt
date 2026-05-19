package com.echo.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EchoRecord::class], version = 1, exportSchema = false)
abstract class EchoDatabase : RoomDatabase() {

    abstract fun echoRecordDao(): EchoRecordDao

    companion object {
        fun create(context: Context): EchoDatabase =
            Room.databaseBuilder(
                context,
                EchoDatabase::class.java,
                "echo.db"
            ).build()
    }
}
