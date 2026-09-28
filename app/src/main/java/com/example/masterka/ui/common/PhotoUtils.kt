package com.example.masterka.ui.common

import android.content.Context
import android.net.Uri
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