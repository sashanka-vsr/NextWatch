package com.nextwatch.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MediaItem::class,
        MediaGenre::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class NextWatchDatabase : RoomDatabase() {

    abstract fun mediaDao(): MediaDao

    abstract fun mediaGenreDao(): MediaGenreDao
}