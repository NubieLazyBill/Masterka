package com.example.masterka.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [StorageNode::class, Category::class, Space::class],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storageDao(): StorageDao
    abstract fun categoryDao(): CategoryDao
    abstract fun spaceDao(): SpaceDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE nodes ADD COLUMN markerSize REAL NOT NULL DEFAULT 0.05")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE nodes ADD COLUMN polygonJson TEXT")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE nodes ADD COLUMN quantity REAL NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE nodes ADD COLUMN unit TEXT NOT NULL DEFAULT 'шт'")
                db.execSQL("ALTER TABLE nodes ADD COLUMN minQuantity REAL")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE nodes ADD COLUMN category TEXT")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    INSERT INTO nodes (name, type, parentId, x, y, markerSize, quantity, unit, createdAt)
                    SELECT 'Мастерская', 'ZONE', NULL, 0, 0, 0.05, 1, 'шт', strftime('%s','now') * 1000
                    WHERE NOT EXISTS (SELECT 1 FROM nodes WHERE parentId IS NULL AND name = 'Мастерская')
                """.trimIndent())
                db.execSQL("""
                    UPDATE nodes
                    SET parentId = (SELECT id FROM nodes WHERE parentId IS NULL AND name = 'Мастерская' LIMIT 1)
                    WHERE parentId IS NULL AND name != 'Мастерская'
                """.trimIndent())
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE nodes ADD COLUMN photoPathsJson TEXT")
                db.execSQL("ALTER TABLE nodes ADD COLUMN photoIndex INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE nodes ADD COLUMN lentTo TEXT")
                db.execSQL("ALTER TABLE nodes ADD COLUMN lentAt INTEGER")
                db.execSQL("ALTER TABLE nodes ADD COLUMN returnBy INTEGER")
                db.execSQL("ALTER TABLE nodes ADD COLUMN noteUpdatedAt INTEGER")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `iconName` TEXT NOT NULL DEFAULT 'Category',
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO categories (name, iconName, createdAt)
                    SELECT DISTINCT category, 'Category', strftime('%s','now')*1000
                    FROM nodes
                    WHERE category IS NOT NULL AND category != ''
                """.trimIndent())
            }
        }

        // ==== МИГРАЦИЯ 9 → 10: пространства ====
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Создаём таблицу spaces
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `spaces` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `iconName` TEXT NOT NULL DEFAULT 'Warehouse',
                        `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                // 2. Добавляем spaceId в nodes
                db.execSQL("ALTER TABLE nodes ADD COLUMN spaceId INTEGER")

                // 3. Создаём пространство по умолчанию «Мастерская», если его ещё нет
                db.execSQL("""
                    INSERT INTO spaces (name, iconName, createdAt)
                    SELECT 'Мастерская', 'Warehouse', strftime('%s','now') * 1000
                    WHERE NOT EXISTS (SELECT 1 FROM spaces)
                """.trimIndent())

                // 4. Привязываем все корневые узлы (parentId IS NULL) к этому пространству
                db.execSQL("""
                    UPDATE nodes
                    SET spaceId = (SELECT id FROM spaces ORDER BY id LIMIT 1)
                    WHERE parentId IS NULL
                """.trimIndent())

                // 5. Проставляем spaceId у всех потомков — пробегаем по дереву рекурсивно.
                //    SQLite не умеет WITH RECURSIVE на старых версиях, но у тебя minSdk 26 — умеет.
                db.execSQL("""
                    WITH RECURSIVE tree(id, spaceId) AS (
                        SELECT id, spaceId FROM nodes WHERE parentId IS NULL
                        UNION ALL
                        SELECT n.id, t.spaceId
                        FROM nodes n
                        JOIN tree t ON n.parentId = t.id
                    )
                    UPDATE nodes
                    SET spaceId = (SELECT spaceId FROM tree WHERE tree.id = nodes.id)
                    WHERE spaceId IS NULL
                """.trimIndent())
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "masterka.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10
                    )
                    .build()
                    .also { INSTANCE = it }
            }

        fun close() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}