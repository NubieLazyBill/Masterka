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

    // Старые поля — оставляем, используем как "центр" полигона
    // (нужен для совместимости и для вещей-без-фото)
    val x: Float = 0f,
    val y: Float = 0f,
    val markerSize: Float = 0.05f,

    // НОВОЕ: полигон в JSON — список точек [{x,y},{x,y},...], координаты в долях [0..1]
    val polygonJson: String? = null,

    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)