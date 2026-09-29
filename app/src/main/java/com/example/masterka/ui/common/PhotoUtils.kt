package com.example.masterka.ui.common

import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/**
 * Копирует выбранное фото из галереи во внутреннюю память приложения.
 * Возвращает абсолютный путь к скопированному файлу.
 */
fun copyPhotoToInternal(context: Context, uri: Uri): String {
    val dir = File(context.filesDir, "photos").apply { if (!exists()) mkdirs() }
    val file = File(dir, "img_${UUID.randomUUID()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return file.absolutePath
}

/**
 * Создаёт временный файл в кэше для съёмки камерой.
 */
fun createTempCameraFile(context: Context): File {
    val dir = File(context.cacheDir, "camera").apply { if (!exists()) mkdirs() }
    return File(dir, "photo_${System.currentTimeMillis()}.jpg")
}

/**
 * Получает content:// URI для файла через FileProvider.
 */
fun getUriForFile(context: Context, file: File): Uri {
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

/**
 * Проверяет, выдано ли разрешение CAMERA.
 */
fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.CAMERA
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}