package com.echo.app.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.echo.app.R
import com.echo.app.data.model.ECHO_EMOJIS
import com.echo.app.ui.trend.EmojiDistribution
import java.io.File
import java.io.FileOutputStream

class ShareCardHelper {

    companion object {
        fun shareAsImage(context: Context, distributions: List<EmojiDistribution>, streak: Int, totalCount: Int) {
            val width = 1080
            val height = 1600

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val bgColor = 0xFFFFF5E6.toInt()
            val white = 0xFFFFFFFF.toInt()
            val darkBrown = 0xFF3D352C.toInt()
            val brown = 0xFF8B7355.toInt()
            val lightBrown = 0xFFD4C5B0.toInt()
            val muted = 0xFFA09080.toInt()

            canvas.drawColor(bgColor)

            val titleTypeface = ResourcesCompat.getFont(context, R.font.zcool_qlkh_yellow_you)

            fun paint(
                size: Float,
                color: Int,
                align: Paint.Align = Paint.Align.CENTER,
                alpha: Int = 255,
                typeface: Typeface? = null,
                style: Paint.Style = Paint.Style.FILL
            ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = size
                this.color = color
                this.alpha = alpha
                textAlign = align
                if (typeface != null) this.typeface = typeface
                this.style = style
                isFakeBoldText = false
            }

            fun drawCentered(text: String, size: Float, color: Int, y: Float, tf: Typeface? = null, alpha: Int = 255) {
                canvas.drawText(text, width / 2f, y, paint(size, color, alpha = alpha, typeface = tf))
            }

            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = white
                style = Paint.Style.FILL
            }
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x08000000.toInt()
                style = Paint.Style.FILL
            }
            val cornerR = 20f

            fun drawCard(x: Float, y: Float, w: Float, h: Float) {
                val rect = RectF(x, y, x + w, y + h)
                canvas.drawRoundRect(RectF(x + 3, y + 3, x + w + 3, y + h + 3), cornerR, cornerR, shadowPaint)
                canvas.drawRoundRect(rect, cornerR, cornerR, cardPaint)
            }

            var y = 130f

            // Title
            drawCentered("🎵", 56f, brown, y)
            y += 64f

            drawCentered("回声", 44f, darkBrown, y, titleTypeface)
            y += 90f

            // Emoji cards grid (2 rows x 6 cols)
            val cardW = 130f
            val cardH = 130f
            val gapX = 24f
            val gapY = 24f
            val gridW = 6 * cardW + 5 * gapX
            val startX = (width - gridW) / 2f
            val emojiSize = 52f
            val cardRow1Y = y
            val cardRow2Y = y + cardH + gapY

            ECHO_EMOJIS.forEachIndexed { index, emoji ->
                val col = index % 6
                val row = index / 6
                val cx = startX + col * (cardW + gapX)
                val cy = if (row == 0) cardRow1Y else cardRow2Y
                drawCard(cx, cy, cardW, cardH)
                val emojiPaint = paint(emojiSize, darkBrown)
                val textX = cx + cardW / 2
                val textY = cy + cardH / 2 + emojiSize * 0.35f
                canvas.drawText(emoji.emoji, textX, textY, emojiPaint)
            }

            y = cardRow2Y + cardH + 50f

            // Divider line
            val dividerPaint = Paint().apply {
                color = lightBrown
                strokeWidth = 2f
                style = Paint.Style.STROKE
            }
            canvas.drawLine(80f, y, width - 80f, y, dividerPaint)
            y += 50f

            // Section title
            drawCentered("这个月的情绪关键词", 26f, darkBrown, y)
            y += 50f

            // Keywords line
            if (distributions.isNotEmpty()) {
                val keywords = distributions.take(3).joinToString("  ·  ") { "${it.emoji} ${it.label}" }
                drawCentered(keywords, 30f, brown, y)
                y += 60f
            }

            // Stats cards
            if (distributions.isNotEmpty()) {
                val statCardW = 280f
                val statCardH = 80f
                val statGap = 24f
                val showCount = minOf(distributions.size, 3)
                val totalStatW = showCount * statCardW + (showCount - 1) * statGap
                val statStartX = (width - totalStatW) / 2f

                repeat(showCount) { i ->
                    val d = distributions[i]
                    val sx = statStartX + i * (statCardW + statGap)
                    drawCard(sx, y, statCardW, statCardH)
                    val statPaint = paint(24f, darkBrown)
                    val statText = "${d.emoji}  ${d.count}次"
                    canvas.drawText(statText, sx + statCardW / 2, y + statCardH / 2 + 9f, statPaint)
                }
                y += statCardH + 50f
            }

            // Streak
            if (totalCount > 0) {
                drawCentered("🔥 连续记录 $streak 天", 28f, brown, y)
                y += 60f
            }

            // Footer
            y = height - 100f
            drawCentered("—— 来自「回声」App ——", 18f, muted, y, alpha = 100)

            saveAndToast(context, bitmap)
        }

        private fun saveAndToast(context: Context, bitmap: Bitmap) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "echo_share_${System.currentTimeMillis()}.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                )
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                }
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val file = File(dir, "echo_share_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
            }
            Toast.makeText(context, "已保存到相册", Toast.LENGTH_SHORT).show()
        }
    }
}
