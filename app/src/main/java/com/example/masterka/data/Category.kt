package com.example.masterka.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                     // "Инструмент"
    val iconName: String = "Category",    // имя иконки из Material Icons
    val createdAt: Long = System.currentTimeMillis()
)