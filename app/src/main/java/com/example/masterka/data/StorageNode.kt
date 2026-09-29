package com.example.masterka.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class NodeType {
    ZONE,
    CONTAINER,
    ITEM
}

@Entity(
    tableName = "nodes",
    foreignKeys = [
        ForeignKey(
            entity = StorageNode::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parentId"), Index("name")]
)
data class StorageNode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parentId: Long? = null,
    val name: String,
    val type: NodeType,
    val photoPath: String? = null,
    val x: Float = 0f,
    val y: Float = 0f,
    val markerSize: Float = 0.05f,
    val polygonJson: String? = null,
    val note: String? = null,
    val category: String? = null,   // "инструмент", "расходник", "метизы", ...
    val photoPathsJson: String? = null,   // JSON-массив путей ["path1","path2",...]
    val photoIndex: Int = 0,              // для контейнеров: на каком фото они нарисованы

    // ==== НОВОЕ: учёт количества ====
    val quantity: Float = 1f,
    val unit: String = "шт",
    val minQuantity: Float? = null,

    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Возвращает список всех фото узла.
 * Если есть photoPathsJson — использует его.
 * Иначе — fallback на старый одиночный photoPath.
 */
fun StorageNode.allPhotoPaths(): List<String> {
    val fromJson = PhotoPathsCodec.decode(photoPathsJson)
    return if (fromJson.isNotEmpty()) fromJson
    else photoPath?.let { listOf(it) } ?: emptyList()
}

/**
 * «Много» — quantity == -1. Отображается как «много» в UI.
 */
fun StorageNode.isMany(): Boolean = quantity < 0f