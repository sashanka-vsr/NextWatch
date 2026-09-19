package com.nextwatch.app.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {

    @Volatile
    private var instance: NextWatchDatabase? = null

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `watch_providers_cache` (
                    `tmdbId` INTEGER NOT NULL,
                    `type` TEXT NOT NULL,
                    `regionsJson` TEXT NOT NULL,
                    `fetchedAt` INTEGER NOT NULL,
                    PRIMARY KEY(`tmdbId`, `type`)
                )
                """.trimIndent()
            )
        }
    }

    fun getDatabase(context: Context): NextWatchDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                NextWatchDatabase::class.java,
                "nextwatch.db",
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }
    }

    fun mediaDao(context: Context): MediaDao = getDatabase(context).mediaDao()

    fun mediaGenreDao(context: Context): MediaGenreDao {
        return getDatabase(context).mediaGenreDao()
    }

    fun watchProviderDao(context: Context): WatchProviderDao = getDatabase(context).watchProviderDao()
}

