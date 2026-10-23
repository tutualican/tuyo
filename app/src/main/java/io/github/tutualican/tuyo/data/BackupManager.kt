package io.github.tutualican.tuyo.data

import android.content.Context
import android.net.Uri
import io.github.tutualican.tuyo.domain.model.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

/**
 * Local-only backup. A dated copy is written to app storage after every change
 * so a bad edit can be undone without a cloud account or network permission.
 * Manual export/import uses the Android document picker (SAF).
 */
object BackupManager {
    private const val DIR = "backups"
    private const val KEEP = 7

    fun autoBackup(context: Context, data: UserData) {
        runCatching {
            val dir = File(context.filesDir, DIR).apply { mkdirs() }
            val file = File(dir, "tuyo-${LocalDate.now()}.json")
            file.writeText(UserRepository.json.encodeToString(UserData.serializer(), data))
            dir.listFiles()
                ?.filter { it.isFile && it.extension == "json" }
                ?.sortedByDescending { it.name }
                ?.drop(KEEP)
                ?.forEach { it.delete() }
        }
    }

    fun latestAutomaticBackup(context: Context): File? =
        File(context.filesDir, DIR)
            .listFiles()
            ?.filter { it.isFile && it.extension == "json" }
            ?.maxByOrNull { it.name }

    fun clear(context: Context) {
        File(context.filesDir, DIR).deleteRecursively()
    }

    suspend fun export(context: Context, uri: Uri, data: UserData): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val text = UserRepository.json.encodeToString(UserData.serializer(), data)
                context.contentResolver.openOutputStream(uri, "wt")
                    ?.bufferedWriter()
                    ?.use { it.write(text) }
                    ?: error("Dosya açılamadı")
            }
        }

    suspend fun import(context: Context, uri: Uri): Result<UserData> =
        withContext(Dispatchers.IO) {
            runCatching {
                val text = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: error("Dosya okunamadı")
                UserRepository.json.decodeFromString(UserData.serializer(), text)
            }
        }
}
