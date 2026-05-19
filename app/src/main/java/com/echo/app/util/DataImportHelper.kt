package com.echo.app.util

import android.content.Context
import android.net.Uri
import com.echo.app.data.db.EchoRecord
import com.echo.app.data.db.EchoRecordDao
import org.json.JSONArray
import org.json.JSONObject

class DataImportHelper {

    companion object {
        suspend fun importFromJson(context: Context, uri: Uri, dao: EchoRecordDao): Int {
            val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().readText()
            } ?: return 0

            val root = JSONObject(content)
            if (!root.has("app") || !root.has("records")) {
                throw IllegalArgumentException("文件格式不正确，请选择回声导出的 JSON 文件")
            }
            val jsonArray = root.getJSONArray("records")
            val existingDates = mutableSetOf<Long>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val dateMillis = obj.getLong("dateMillis")
                val existing = dao.getByDate(dateMillis)
                if (existing != null) {
                    existingDates.add(dateMillis)
                }
            }

            var imported = 0
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val dateMillis = obj.getLong("dateMillis")
                if (dateMillis in existingDates) continue

                val record = EchoRecord(
                    emoji = obj.getString("emoji"),
                    label = obj.getString("label"),
                    dateMillis = dateMillis,
                    createdAtMillis = obj.optLong("createdAtMillis", dateMillis)
                )
                dao.insert(record)
                imported++
            }

            return imported
        }
    }
}
