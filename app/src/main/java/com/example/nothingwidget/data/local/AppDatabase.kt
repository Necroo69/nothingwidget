package com.example.nothingwidget.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WidgetConfigEntity::class,
        StepEntryEntity::class,
        SavedNoteEntity::class,
        WidgetInstanceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun widgetConfigDao(): WidgetConfigDao
    abstract fun widgetInstanceDao(): WidgetInstanceDao
    abstract fun stepDao(): StepDao
    abstract fun noteDao(): NoteDao

    companion object {
        // v2 adds per-placed-widget configs (B3). Only adds a table, so no saved data is lost.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `widget_instances` (" +
                        "`appWidgetId` INTEGER NOT NULL, `presetId` TEXT NOT NULL, " +
                        "`isCustomized` INTEGER NOT NULL, `accentColorHex` TEXT NOT NULL, " +
                        "`isMonochrome` INTEGER NOT NULL, `cornerRadiusDp` INTEGER NOT NULL, " +
                        "`transparencyPercent` INTEGER NOT NULL, " +
                        "`showDotMatrixBackground` INTEGER NOT NULL, " +
                        "`showGlyphBorder` INTEGER NOT NULL, `customSubtitle` TEXT NOT NULL, " +
                        "PRIMARY KEY(`appWidgetId`))"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nothing_widgets.db"
                ).addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
