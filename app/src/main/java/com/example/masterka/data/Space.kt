package com.example.masterka.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Помещение (мастерская, гараж, кладовка и т.п.).
 * Верхний уровень иерархии: Space → ZONE → CONTAINER → ITEM.
 */
@Entity(tableName = "spaces")
data class Space(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String = "Warehouse",   // задел на иконку
    val createdAt: Long = System.currentTimeMillis()
)