package com.example.masterka.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [StorageNode::class, Category::class],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storageDao(): StorageDao
    abstract fun categoryDao(): CategoryDao

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
                        MIGRATION_8_9
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