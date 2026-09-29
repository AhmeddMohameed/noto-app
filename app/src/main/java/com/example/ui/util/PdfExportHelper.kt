package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.domain.model.Note
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportHelper {

    fun exportAndSharePdf(context: Context, note: Note) {
        val pdfFile = createPdfFile(context, note) ?: return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, note.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share Note as PDF"))
    }

    private fun createPdfFile(context: Context, note: Note): File? {
        val document = PdfDocument()
        val pageWidth = 595 // Standard A4 width in points
        val pageHeight = 842 // Standard A4 height in points
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        var y = 60f
        val margin = 50f
        val contentWidth = pageWidth - (margin * 2)

        // Draw App / Brand Watermark header
        canvas.drawText("Noto Notes", margin, y, metaPaint)
        y += 24f

        // Draw Title
        canvas.drawText(note.title.ifBlank { "Untitled Note" }, margin, y, titlePaint)
        y += 24f

        // Draw Meta (Date, Folder, Tags)
        val formattedDate = SimpleDateFormat("MMMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(note.updatedAt))
        val metaInfo = buildString {
            append(formattedDate)
            if (!note.folderName.isNullOrBlank()) {
                append(" | Folder: ${note.folderName}")
            }
            if (note.tags.isNotEmpty()) {
                append(" | Tags: ${note.tags.joinToString(", ")}")
            }
        }
        canvas.drawText(metaInfo, margin, y, metaPaint)
        y += 20f

        // Divider Line
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1.5f
        }
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
        y += 25f

        // Draw Checklist items if any
        if (note.checklist.isNotEmpty()) {
            val checkPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("Checklist:", margin, y, checkPaint)
            y += 18f

            for (item in note.checklist) {
                if (y > pageHeight - 60f) break
                val checkSymbol = if (item.isChecked) "[✓] " else "[  ] "
                canvas.drawText(checkSymbol + item.text, margin + 10f, y, bodyPaint)
                y += 18f
            }
            y += 10f
        }

        // Draw Content lines with auto-wrap
        val lines = note.content.split("\n")
        for (rawLine in lines) {
            if (y > pageHeight - 60f) break
            if (rawLine.isBlank()) {
                y += 12f
                continue
            }

            // Word wrap
            val words = rawLine.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val textWidth = bodyPaint.measureText(testLine)
                if (textWidth > contentWidth) {
                    canvas.drawText(currentLine, margin, y, bodyPaint)
                    y += 18f
                    currentLine = word
                    if (y > pageHeight - 60f) break
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty() && y <= pageHeight - 60f) {
                canvas.drawText(currentLine, margin, y, bodyPaint)
                y += 18f
            }
        }

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val sanitizedTitle = note.title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(30).ifBlank { "Note" }
        val file = File(outputDir, "${sanitizedTitle}_${note.id}.pdf")

        return try {
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            fos.close()
            document.close()
            file
        } catch (e: Exception) {
            document.close()
            null
        }
    }
}
