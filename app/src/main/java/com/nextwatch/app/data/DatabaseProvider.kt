package com.nextwatch.app.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var instance: NextWatchDatabase? = null

    fun getDatabase(context: Context): NextWatchDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                NextWatchDatabase::class.java,
                "nextwatch.db",
            ).build().also { instance = it }
        }
    }

    fun mediaDao(context: Context): MediaDao = getDatabase(context).mediaDao()

    fun mediaGenreDao(context: Context): MediaGenreDao {
        return getDatabase(context).mediaGenreDao()
    }
}
