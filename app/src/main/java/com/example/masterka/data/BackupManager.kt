package com.example.masterka.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {

    private const val DB_NAME = "masterka.db"
    private const val PHOTOS_DIR = "photos"
    private const val META_NAME = "meta.json"
    private const val TAG = "MasterkaBackup"

    /**
     * Экспорт: создаёт zip-архив с БД + фото и пишет в uri.
     */
    suspend fun export(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("База данных не найдена"))
            }

            // Закрываем БД, чтобы файл был консистентным
            AppDatabase.close()

            val photosDir = File(context.filesDir, PHOTOS_DIR)

            context.contentResolver.openOutputStream(uri)?.use { out ->
                ZipOutputStream(out).use { zip ->
                    // 1) БД
                    zip.putNextEntry(ZipEntry(DB_NAME))
                    dbFile.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()

                    // 2) Фото
                    if (photosDir.exists()) {
                        photosDir.listFiles()?.forEach { file ->
                            if (file.isFile) {
                                zip.putNextEntry(ZipEntry("$PHOTOS_DIR/${file.name}"))
                                file.inputStream().use { it.copyTo(zip) }
                                zip.closeEntry()
                            }
                        }
                    }

                    // 3) meta.json
                    val meta = JSONObject().apply {
                        put("version", 1)
                        put("exportedAt", System.currentTimeMillis())
                        put(
                            "exportedAtHuman",
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                .format(Date())
                        )
                    }
                    zip.putNextEntry(ZipEntry(META_NAME))
                    zip.write(meta.toString(2).toByteArray())
                    zip.closeEntry()
                }
            } ?: return@withContext Result.failure(Exception("Не удалось открыть поток записи"))

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Ошибка экспорта", e)
            Result.failure(e)
        }
    }

    /**
     * Импорт: распаковывает zip и заменяет БД + фото.
     * ⚠️ ВСЕ СУЩЕСТВУЮЩИЕ ДАННЫЕ БУДУТ УДАЛЕНЫ.
     */
    suspend fun import(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            val photosDir = File(context.filesDir, PHOTOS_DIR)

            // Закрываем БД перед заменой
            AppDatabase.close()

            // Временная папка
            val tempDir = File(context.cacheDir, "import_temp").apply {
                if (exists()) deleteRecursively()
                mkdirs()
            }

            // 1) Распаковка zip
            context.contentResolver.openInputStream(uri)?.use { input ->
                unzip(input, tempDir)
            } ?: return@withContext Result.failure(Exception("Не удалось открыть архив"))

            // 2) Проверка БД
            val tempDb = File(tempDir, DB_NAME)
            if (!tempDb.exists()) {
                tempDir.deleteRecursively()
                return@withContext Result.failure(Exception("В архиве нет $DB_NAME"))
            }

            // 3) Удаляем старую БД и её вспомогательные файлы
            if (dbFile.exists()) dbFile.delete()
            File(dbFile.path + "-wal").takeIf { it.exists() }?.delete()
            File(dbFile.path + "-shm").takeIf { it.exists() }?.delete()

            // 4) Копируем новую БД
            dbFile.parentFile?.mkdirs()
            tempDb.copyTo(dbFile, overwrite = true)

            // 5) Удаляем старые фото и копируем новые
            if (photosDir.exists()) photosDir.deleteRecursively()
            photosDir.mkdirs()

            val tempPhotos = File(tempDir, PHOTOS_DIR)
            if (tempPhotos.exists()) {
                tempPhotos.listFiles()?.forEach { file ->
                    if (file.isFile) file.copyTo(File(photosDir, file.name), overwrite = true)
                }
            }

            // 6) Чистим временную папку
            tempDir.deleteRecursively()

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Ошибка импорта", e)
            Result.failure(e)
        }
    }

    private fun unzip(input: InputStream, targetDir: File) {
        ZipInputStream(input).use { zip ->
            var entry: ZipEntry? = zip.nextEntry
            while (entry != null) {
                val file = File(targetDir, entry.name)
                // Защита от zip-slip
                if (!file.canonicalPath.startsWith(targetDir.canonicalPath)) {
                    throw SecurityException("Опасная запись в архиве: ${entry.name}")
                }
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    file.outputStream().use { out -> zip.copyTo(out) }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }

    fun suggestFileName(): String {
        val stamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
        return "masterka_backup_$stamp.zip"
    }
}