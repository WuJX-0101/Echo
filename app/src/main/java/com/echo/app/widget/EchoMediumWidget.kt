package com.echo.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.echo.app.MainActivity
import com.echo.app.data.db.EchoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

class EchoMediumWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val weekData = withContext(Dispatchers.IO) {
            val dao = EchoDatabase.create(context).echoRecordDao()
            val today = LocalDate.now(ZoneId.systemDefault())
            val weekAgo = today.minusDays(6)
            val startMillis = weekAgo.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val records = dao.observeRange(startMillis, endMillis).first()

            val dayLabels = listOf("一", "二", "三", "四", "五", "六", "日")
            val dayOfWeek = today.dayOfWeek.value
            val shiftedLabels = (0 until 7).map { i ->
                val idx = ((dayOfWeek - 1 - (6 - i)) % 7 + 7) % 7
                dayLabels[idx]
            }

            (0 until 7).map { i ->
                val day = weekAgo.plusDays(i.toLong())
                val dayMillis = day.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val emoji = records.find { it.dateMillis == dayMillis }?.emoji ?: "·"
                Pair(emoji, shiftedLabels[i])
            }
        }

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.background)
                        .padding(12)
                        .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = "回声",
                        style = TextStyle(fontSize = 14.sp)
                    )
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                    ) {
                        weekData.forEach { (emoji, _) ->
                            Text(
                                text = emoji,
                                style = TextStyle(fontSize = 18.sp),
                                modifier = GlanceModifier.padding(4)
                            )
                        }
                    }
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                    ) {
                        weekData.forEach { (_, label) ->
                            Text(
                                text = label,
                                style = TextStyle(fontSize = 10.sp),
                                modifier = GlanceModifier.padding(4)
                            )
                        }
                    }
                    Text(
                        text = "点击记录今天",
                        style = TextStyle(fontSize = 11.sp)
                    )
                }
            }
        }
    }
}
