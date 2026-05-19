package com.echo.app.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.echo.app.data.db.EchoRecord
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class DataExportHelper {

    companion object {
        fun exportToJson(context: Context, records: List<EchoRecord>): String? {
            val jsonArray = JSONArray()
            records.forEach { record ->
                val obj = JSONObject().apply {
                    put("emoji", record.emoji)
                    put("label", record.label)
                    put("dateMillis", record.dateMillis)
                    put("createdAtMillis", record.createdAtMillis)
                }
                jsonArray.put(obj)
            }

            val root = JSONObject().apply {
                put("app", "回声")
                put("version", "1.0")
                put("exportDate", System.currentTimeMillis())
                put("totalCount", records.size)
                put("records", jsonArray)
            }

            val fileName = "echo_data_${System.currentTimeMillis()}.json"
            val content = root.toString(2)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/json")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                )
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(content.toByteArray())
                    }
                    return fileName
                }
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(dir, fileName)
                FileOutputStream(file).use { stream ->
                    stream.write(content.toByteArray())
                }
                return fileName
            }
            return null
        }
    }
}
