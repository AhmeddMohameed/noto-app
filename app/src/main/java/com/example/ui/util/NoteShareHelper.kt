package com.example.ui.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.domain.model.ChecklistItem
import com.example.domain.model.Folder
import com.example.domain.model.Note
import com.example.domain.model.NoteType
import com.example.domain.model.Tag
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object NoteShareHelper {

    fun shareAsPlainText(context: Context, note: Note) {
        val shareBody = buildString {
            appendLine(note.title)
            appendLine("=".repeat(note.title.length.coerceAtLeast(10)))
            if (note.checklist.isNotEmpty()) {
                appendLine()
                appendLine("Checklist:")
                note.checklist.forEach {
                    val mark = if (it.isChecked) "[✓]" else "[ ]"
                    appendLine("$mark ${it.text}")
                }
            }
            if (note.content.isNotBlank()) {
                appendLine()
                appendLine(note.content)
            }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, note.title)
            putExtra(Intent.EXTRA_TEXT, shareBody)
        }
        context.startActivity(Intent.createChooser(intent, "Share Note"))
    }

    fun shareAsMarkdownFile(context: Context, note: Note) {
        val content = buildString {
            appendLine("# ${note.title}")
            appendLine()
            if (note.tags.isNotEmpty()) {
                appendLine("Tags: ${note.tags.joinToString(" ") { "#$it" }}")
                appendLine()
            }
            if (note.checklist.isNotEmpty()) {
                appendLine("### Tasks")
                note.checklist.forEach {
                    val mark = if (it.isChecked) "- [x]" else "- [ ]"
                    appendLine("$mark ${it.text}")
                }
                appendLine()
            }
            appendLine(note.content)
        }

        val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val sanitizedTitle = note.title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(30).ifBlank { "Note" }
        val file = File(outputDir, "$sanitizedTitle.md")
        file.writeText(content)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/markdown"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Markdown File"))
    }

    fun exportFullBackup(
        context: Context,
        notes: List<Note>,
        folders: List<Folder>,
        tags: List<Tag>
    ): File? {
        return try {
            val root = JSONObject()
            root.put("version", 1)
            root.put("timestamp", System.currentTimeMillis())
            root.put("appName", "Noto")

            // Notes
            val notesArr = JSONArray()
            for (note in notes) {
                val nObj = JSONObject()
                nObj.put("id", note.id)
                nObj.put("title", note.title)
                nObj.put("content", note.content)
                nObj.put("createdAt", note.createdAt)
                nObj.put("updatedAt", note.updatedAt)
                nObj.put("isPinned", note.isPinned)
                nObj.put("isFavorite", note.isFavorite)
                nObj.put("isArchived", note.isArchived)
                nObj.put("color", note.color)
                nObj.put("noteType", note.noteType.name)
                nObj.put("folderName", note.folderName ?: "")
                nObj.put("tags", JSONArray(note.tags))

                val checkArr = JSONArray()
                note.checklist.forEach {
                    val cObj = JSONObject()
                    cObj.put("id", it.id)
                    cObj.put("text", it.text)
                    cObj.put("isChecked", it.isChecked)
                    checkArr.put(cObj)
                }
                nObj.put("checklist", checkArr)
                notesArr.put(nObj)
            }
            root.put("notes", notesArr)

            // Folders
            val foldersArr = JSONArray()
            for (f in folders) {
                val fObj = JSONObject()
                fObj.put("id", f.id)
                fObj.put("name", f.name)
                fObj.put("icon", f.icon)
                fObj.put("color", f.color)
                foldersArr.put(fObj)
            }
            root.put("folders", foldersArr)

            // Tags
            val tagsArr = JSONArray()
            for (t in tags) {
                val tObj = JSONObject()
                tObj.put("name", t.name)
                tObj.put("color", t.color)
                tagsArr.put(tObj)
            }
            root.put("tags", tagsArr)

            val dir = File(context.cacheDir, "backups").apply { mkdirs() }
            val backupFile = File(dir, "noto_backup_${System.currentTimeMillis()}.json")
            FileOutputStream(backupFile).use {
                it.write(root.toString(2).toByteArray())
            }
            backupFile
        } catch (e: Exception) {
            null
        }
    }

    fun parseBackupJson(jsonString: String): Triple<List<Note>, List<Folder>, List<Tag>> {
        val root = JSONObject(jsonString)
        val notes = mutableListOf<Note>()
        val folders = mutableListOf<Folder>()
        val tags = mutableListOf<Tag>()

        if (root.has("notes")) {
            val arr = root.getJSONArray("notes")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val checkList = mutableListOf<ChecklistItem>()
                if (obj.has("checklist")) {
                    val cArr = obj.getJSONArray("checklist")
                    for (j in 0 until cArr.length()) {
                        val cObj = cArr.getJSONObject(j)
                        checkList.add(
                            ChecklistItem(
                                id = cObj.optString("id", java.util.UUID.randomUUID().toString()),
                                text = cObj.optString("text", ""),
                                isChecked = cObj.optBoolean("isChecked", false)
                            )
                        )
                    }
                }
                val tagsList = mutableListOf<String>()
                if (obj.has("tags")) {
                    val tArr = obj.getJSONArray("tags")
                    for (k in 0 until tArr.length()) {
                        tagsList.add(tArr.getString(k))
                    }
                }

                notes.add(
                    Note(
                        title = obj.optString("title", "Untitled"),
                        content = obj.optString("content", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                        isPinned = obj.optBoolean("isPinned", false),
                        isFavorite = obj.optBoolean("isFavorite", false),
                        isArchived = obj.optBoolean("isArchived", false),
                        color = obj.optLong("color", 0L),
                        noteType = try {
                            NoteType.valueOf(obj.optString("noteType", "STANDARD"))
                        } catch (_: Exception) {
                            NoteType.STANDARD
                        },
                        checklist = checkList,
                        tags = tagsList,
                        folderName = obj.optString("folderName").takeIf { it.isNotBlank() }
                    )
                )
            }
        }

        if (root.has("folders")) {
            val fArr = root.getJSONArray("folders")
            for (i in 0 until fArr.length()) {
                val obj = fArr.getJSONObject(i)
                folders.add(
                    Folder(
                        name = obj.optString("name", "Folder"),
                        icon = obj.optString("icon", "folder"),
                        color = obj.optLong("color", 0xFF4F46E5)
                    )
                )
            }
        }

        if (root.has("tags")) {
            val tArr = root.getJSONArray("tags")
            for (i in 0 until tArr.length()) {
                val obj = tArr.getJSONObject(i)
                tags.add(
                    Tag(
                        name = obj.optString("name", "Tag"),
                        color = obj.optLong("color", 0xFF3B82F6)
                    )
                )
            }
        }

        return Triple(notes, folders, tags)
    }
}
