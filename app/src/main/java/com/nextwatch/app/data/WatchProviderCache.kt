package com.nextwatch.app.data

import androidx.room.Entity

@Entity(tableName = "watch_providers_cache", primaryKeys = ["tmdbId", "type"])
data class WatchProviderCache(
    val tmdbId: Int,
    val type: String,        // MediaItem.TYPE_MOVIE or MediaItem.TYPE_SERIES
    val regionsJson: String, // raw "results" object from TMDB, as text
    val fetchedAt: Long,
)
