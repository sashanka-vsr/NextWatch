package com.nextwatch.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaGenreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(genres: List<MediaGenre>)

    @Query("DELETE FROM media_genres WHERE mediaId = :mediaId")
    suspend fun deleteForMedia(mediaId: Long)

    @Query("SELECT genre FROM media_genres WHERE mediaId = :mediaId")
    fun observeGenres(mediaId: Long): Flow<List<String>>

    @Query("SELECT genre FROM media_genres WHERE mediaId = :mediaId")
    suspend fun getGenres(mediaId: Long): List<String>

    @Query(
        """
        SELECT mediaId
        FROM media_genres
        WHERE genre = :genre
        """
    )
    suspend fun getMediaIdsForGenre(genre: String): List<Long>

    /** Returns all (mediaId, genre) rows for the given IDs — used for bulk in-memory filtering. */
    @Query("SELECT mediaId, genre FROM media_genres WHERE mediaId IN (:mediaIds)")
    fun observeGenresForMediaIds(mediaIds: List<Long>): Flow<List<MediaGenre>>

    @Query("SELECT * FROM media_genres")
    suspend fun getAllGenres(): List<MediaGenre>

    @Query("DELETE FROM media_genres")
    suspend fun deleteAll()
}