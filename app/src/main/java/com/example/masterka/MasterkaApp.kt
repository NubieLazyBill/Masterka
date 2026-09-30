package com.example.masterka

import android.app.Application
import com.example.masterka.data.AppDatabase
import com.example.masterka.data.Category
import com.example.masterka.data.Space
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MasterkaApp : Application() {

    val db by lazy { AppDatabase.get(this) }
    val dao by lazy { db.storageDao() }
    val categoryDao by lazy { db.categoryDao() }
    val spaceDao by lazy { db.spaceDao() }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        appScope.launch {
            // ==== Синхронизация категорий ====
            val existing = categoryDao.getAllCategoriesOnce().map { it.name }.toSet()
            dao.getAllCategoriesOnce()
                .filter { it.isNotBlank() && it !in existing }
                .forEach { name ->
                    categoryDao.insert(Category(name = name, iconName = "Category"))
                }

            // ==== Страховка: если spaces пусто — создаём «Мастерскую» ====
            if (spaceDao.getAllOnce().isEmpty()) {
                spaceDao.insert(Space(name = "Мастерская"))
            }
        }
    }
}