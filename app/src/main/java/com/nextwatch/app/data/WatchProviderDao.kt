package com.nextwatch.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WatchProviderDao {
    @Query("SELECT * FROM watch_providers_cache WHERE tmdbId = :tmdbId AND type = :type LIMIT 1")
    suspend fun get(tmdbId: Int, type: String): WatchProviderCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(cache: WatchProviderCache)
}
