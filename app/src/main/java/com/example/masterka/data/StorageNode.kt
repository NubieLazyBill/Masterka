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

    // ==== НОВОЕ: учёт количества ====
    val quantity: Float = 1f,
    val unit: String = "шт",
    val minQuantity: Float? = null,

    val createdAt: Long = System.currentTimeMillis()
)