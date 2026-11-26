package com.example.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.model.DailyWinnerRecord
import java.io.File
import java.io.FileOutputStream

object ContentExportManager {

    fun renderDailyLeaderboardPostBitmap(
        date: String,
        winners: List<DailyWinnerRecord>
    ): Bitmap {
        val width = 1080
        val height = 1080
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        drawCyberBackground(canvas, width, height)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.parseColor("#00FFA3")
        }
        canvas.drawRoundRect(RectF(28f, 28f, width - 28f, height - 28f), 36f, 36f, borderPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00FFA3")
            textSize = 60f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        canvas.drawText("CHART ARENA", 72f, 128f, titlePaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFB800")
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("DAILY LEADERBOARD  •  $date", 72f, 182f, subPaint)

        val top3 = winners.sortedBy { it.rank }.take(3)
        if (top3.isEmpty()) {
            val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#8B95A5")
                textSize = 36f
            }
            canvas.drawText("No saved winners for $date yet.", 72f, 460f, emptyPaint)
        } else {
            top3.forEachIndexed { idx, winner ->
                val top = 250f + idx * 230f
                drawWinnerPodiumCard(canvas, winner, idx, 72f, top, width - 72f, top + 195f)
            }
        }

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8B95A5")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("CYBERPUNK TRADING PREDICTION ARCADE  •  PRO ARENA", 72f, 1005f, footerPaint)

        return bmp
    }

    fun renderInstagramStoryBitmap(
        date: String,
        winners: List<DailyWinnerRecord>
    ): Bitmap {
        val width = 1080
        val height = 1920
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        drawCyberBackground(canvas, width, height)

        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.parseColor("#FFB800")
        }
        canvas.drawRoundRect(RectF(32f, 44f, width - 32f, height - 44f), 42f, 42f, framePaint)

        val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00FFA3")
            textSize = 54f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("CHART ARENA", width / 2f, 155f, logoPaint)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFB800")
            textSize = 70f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("DAILY CHAMPIONS", width / 2f, 260f, headerPaint)

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8B95A5")
            textSize = 34f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(date, width / 2f, 320f, datePaint)

        val top3 = winners.sortedBy { it.rank }.take(3)
        if (top3.isEmpty()) {
            val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#8B95A5")
                textSize = 38f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("No saved winners for $date", width / 2f, 760f, emptyPaint)
        } else {
            top3.forEachIndexed { idx, winner ->
                val top = 400f + idx * 285f
                drawWinnerPodiumCard(canvas, winner, idx, 68f, top, width - 68f, top + 245f)
            }
        }

        // Next Challenge & Pro Promo Card at bottom
        val promoCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#131B2B")
        }
        val promoRect = RectF(68f, 1340f, width - 68f, 1790f)
        canvas.drawRoundRect(promoRect, 32f, 32f, promoCardPaint)

        val promoStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = Color.parseColor("#00FFA3")
        }
        canvas.drawRoundRect(promoRect, 32f, 32f, promoStroke)

        val promoTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00FFA3")
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("NEXT CHALLENGE DROPPING SOON", width / 2f, 1435f, promoTitle)

        val promoBody = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F3F6FC")
            textSize = 33f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Think you can survive NFP Whipsaws", width / 2f, 1525f, promoBody)
        canvas.drawText("& Institutional Liquidity Sweeps?", width / 2f, 1575f, promoBody)

        val proBadge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFB800")
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("UNLOCK PRO ARENA • ₹199/mo or $14.99/mo", width / 2f, 1690f, proBadge)
        canvas.drawText("DM TO JOIN MONTHLY CHART SNIPER CUP", width / 2f, 1745f, datePaint)

        return bmp
    }

    private fun drawCyberBackground(canvas: Canvas, width: Int, height: Int) {
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                Color.parseColor("#06080D"),
                Color.parseColor("#0F1624"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val gridPaint = Paint().apply {
            color = Color.parseColor("#182236")
            strokeWidth = 2f
        }
        var x = 0f
        while (x < width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
            x += 90f
        }
        var y = 0f
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            y += 90f
        }
    }

    private fun drawWinnerPodiumCard(
        canvas: Canvas,
        winner: DailyWinnerRecord,
        idx: Int,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        val accentColor = when (idx) {
            0 -> Color.parseColor("#FFB800") // Gold
            1 -> Color.parseColor("#D4DCE8") // Silver
            else -> Color.parseColor("#E08E55") // Bronze
        }
        val cardFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#121928")
        }
        val rect = RectF(left, top, right, bottom)
        canvas.drawRoundRect(rect, 28f, 28f, cardFill)

        val cardBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = accentColor
        }
        canvas.drawRoundRect(rect, 28f, 28f, cardBorder)

        val rankBadge = when (winner.rank) {
            1 -> "#1 GOLD"
            2 -> "#2 SILVER"
            else -> "#${winner.rank} BRONZE"
        }
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(rankBadge, left + 36f, top + 58f, badgePaint)

        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F3F6FC")
            textSize = 46f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(winner.playerName, left + 36f, top + 122f, namePaint)

        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8B95A5")
            textSize = 28f
        }
        val handle = winner.instagramHandle.ifBlank { winner.gameMode }
        canvas.drawText(
            "$handle  •  ${winner.wins}W/${winner.losses}L  •  Streak ${winner.streak}",
            left + 36f,
            top + 170f,
            metaPaint
        )

        val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00FFA3")
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("+${winner.score}", right - 36f, top + 118f, scorePaint)
    }

    fun buildLeaderboardPlainText(date: String, winners: List<DailyWinnerRecord>): String {
        val top = winners.sortedBy { it.rank }
        if (top.isEmpty()) return "CHART ARENA LEADERBOARD ($date) - No saved records."
        return buildString {
            appendLine("CHART ARENA OFFICIAL LEADERBOARD ($date)")
            top.forEach { w ->
                val medal = when (w.rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> "#${w.rank}"
                }
                appendLine("$medal ${w.playerName} | Score: ${w.score} | ${w.wins}W-${w.losses}L | Streak: ${w.streak}")
            }
        }.trim()
    }

    fun buildDefaultInstagramCaption(date: String, winners: List<DailyWinnerRecord>): String {
        val top3 = winners.sortedBy { it.rank }.take(3)
        val p1 = top3.getOrNull(0)?.let { "${it.playerName} | ${it.score}" } ?: "TBD | -"
        val p2 = top3.getOrNull(1)?.let { "${it.playerName} | ${it.score}" } ?: "TBD | -"
        val p3 = top3.getOrNull(2)?.let { "${it.playerName} | ${it.score}" } ?: "TBD | -"

        return """
            🏆 CHART ARENA DAILY CHAMPIONS! ($date)
            Today's top chart snipers have been revealed!
            🥇 $p1
            🥈 $p2
            🥉 $p3
            Think you can beat them?
            Join CHART ARENA and prove your trading skills.
            DM us to access Pro Challenges.
            #ChartArena #TradingChallenge #TradingSkills #ChartSniper
        """.trimIndent()
    }

    fun saveBitmapToGalleryOrShare(
        context: Context,
        bitmap: Bitmap,
        fileNamePrefix: String
    ): Result<String> {
        return runCatching {
            val fileName = "${fileNamePrefix}_${System.currentTimeMillis()}.png"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ChartArena")
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    return@runCatching "Saved PNG to Pictures/ChartArena/$fileName"
                }
            }
            val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val outFile = File(exportsDir, fileName)
            FileOutputStream(outFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            shareFile(context, outFile, "image/png", "Share Chart Arena Graphic")
            "Prepared PNG for export: $fileName"
        }
    }

    fun shareBitmap(context: Context, bitmap: Bitmap, fileNamePrefix: String) {
        runCatching {
            val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val outFile = File(exportsDir, "${fileNamePrefix}_${System.currentTimeMillis()}.png")
            FileOutputStream(outFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            shareFile(context, outFile, "image/png", "Share to Instagram / Apps")
        }
    }

    fun shareTextFile(context: Context, content: String, fileName: String, mimeType: String) {
        runCatching {
            val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val outFile = File(exportsDir, fileName)
            outFile.writeText(content)
            shareFile(context, outFile, mimeType, "Export $fileName")
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }
}
