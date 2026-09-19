package com.nextwatch.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: MediaItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: MediaItem): Long

    @Update
    suspend fun update(item: MediaItem)

    @Delete
    suspend fun delete(item: MediaItem)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM media_items ORDER BY dateAdded DESC")
    fun getAll(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE status = :status ORDER BY dateAdded DESC")
    fun getByStatus(status: String): Flow<List<MediaItem>>

    @Query(
        "SELECT * FROM media_items WHERE status = '${MediaItem.STATUS_WATCHLIST}' ORDER BY dateAdded DESC"
    )
    fun getWatchlist(): Flow<List<MediaItem>>

    @Query(
        "SELECT * FROM media_items WHERE status = '${MediaItem.STATUS_WATCHING}' ORDER BY dateAdded DESC"
    )
    fun getWatching(): Flow<List<MediaItem>>

    @Query(
        "SELECT * FROM media_items WHERE status = '${MediaItem.STATUS_WATCHED}' ORDER BY dateAdded DESC"
    )
    fun getWatched(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MediaItem?

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<MediaItem?>

    @Query(
        """
        SELECT * FROM media_items
        WHERE type = :type AND status IN (:statuses)
        ORDER BY dateAdded DESC
        """
    )
    fun getByTypeAndStatuses(type: String, statuses: List<String>): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items ORDER BY dateAdded DESC")
    suspend fun getAllItems(): List<MediaItem>

    @Query("DELETE FROM media_items")
    suspend fun deleteAll()
}
