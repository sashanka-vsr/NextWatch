package com.nextwatch.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tmdbId: Int? = null,
    val imdbId: String? = null,
    val title: String,
    val type: String,
    val status: String,
    val posterUrl: String? = null,
    val releaseYear: String? = null,
    val runtime: String? = null,
    val imdbRating: String? = null,
    val rottenTomatoesRating: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
) {
    companion object {
        const val TYPE_MOVIE = "Movie"
        const val TYPE_SERIES = "Series"

        const val STATUS_WATCHLIST = "Watchlist"
        const val STATUS_WATCHING = "Watching"
        const val STATUS_WATCHED = "Watched"
    }
}
