package com.nextwatch.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media_items",
    indices = [
        Index(
            value = ["tmdbId", "type"],
            unique = true
        )
    ]
)
data class MediaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val tmdbId: Int? = null,
    val imdbId: String? = null,

    val title: String,
    val type: String,
    val status: String,

    val overview: String? = null,

    val posterUrl: String? = null,
    val posterLocalPath: String? = null,

    val releaseDate: String? = null,

    val runtimeMinutes: Int? = null,

    val seasonCount: Int? = null,

    val imdbRating: Double? = null,

    val director: String? = null,
    val creator: String? = null,
    val episodeCount: Int? = null,
    val originalLanguage: String? = null,
    val country: String? = null,

    val dateAdded: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_MOVIE = "Movie"
        const val TYPE_SERIES = "Series"

        const val STATUS_WATCHLIST = "Watchlist"
        const val STATUS_WATCHING = "Watching"
        const val STATUS_WATCHED = "Watched"
        const val STATUS_REWATCH = "Rewatch"
    }
}
