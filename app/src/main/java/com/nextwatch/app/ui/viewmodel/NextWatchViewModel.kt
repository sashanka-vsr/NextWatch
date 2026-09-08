package com.nextwatch.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextwatch.app.data.MediaDao
import com.nextwatch.app.data.MediaItem
import com.nextwatch.app.network.NextWatchApiClient
import com.nextwatch.app.network.OmdbDetails
import com.nextwatch.app.network.TmdbMovie
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NextWatchViewModel(
    private val mediaDao: MediaDao,
    private val apiClient: NextWatchApiClient = NextWatchApiClient(),
) : ViewModel() {

    val watchlistMovies: StateFlow<List<MediaItem>> = mediaDao
        .getByTypeAndStatuses(MediaItem.TYPE_MOVIE, WATCHLIST_STATUSES)
        .stateIn(viewModelScope, WhileSubscribed, emptyList())

    val watchlistSeries: StateFlow<List<MediaItem>> = mediaDao
        .getByTypeAndStatuses(MediaItem.TYPE_SERIES, WATCHLIST_STATUSES)
        .stateIn(viewModelScope, WhileSubscribed, emptyList())

    val historyMovies: StateFlow<List<MediaItem>> = mediaDao
        .getByTypeAndStatuses(MediaItem.TYPE_MOVIE, HISTORY_STATUSES)
        .stateIn(viewModelScope, WhileSubscribed, emptyList())

    val historySeries: StateFlow<List<MediaItem>> = mediaDao
        .getByTypeAndStatuses(MediaItem.TYPE_SERIES, HISTORY_STATUSES)
        .stateIn(viewModelScope, WhileSubscribed, emptyList())

    fun addMedia(item: MediaItem) {
        viewModelScope.launch {
            mediaDao.upsert(item)
        }
    }

    suspend fun saveSearchResult(
        media: TmdbMovie,
        status: String,
        details: OmdbDetails? = null,
    ) {
        val omdb = details ?: runCatching {
            apiClient.fetchOmdbDetails(media.title, media.year, media.mediaType)
        }.getOrNull()

        mediaDao.upsert(
            MediaItem(
                tmdbId = media.tmdbId,
                imdbId = omdb?.imdbId,
                title = media.title,
                type = media.mediaType,
                status = status,
                posterUrl = media.posterUrl,
                releaseYear = media.year,
                runtime = omdb?.runtime,
                imdbRating = omdb?.imdbRating,
                rottenTomatoesRating = omdb?.rottenTomatoesRating,
            ),
        )
    }

    fun moveToHistory(item: MediaItem) {
        viewModelScope.launch {
            mediaDao.update(item.copy(status = MediaItem.STATUS_WATCHED))
        }
    }

    fun rewatchMedia(item: MediaItem) {
        viewModelScope.launch {
            mediaDao.update(item.copy(status = MediaItem.STATUS_WATCHLIST))
        }
    }

    private companion object {
        val WATCHLIST_STATUSES = listOf(
            MediaItem.STATUS_WATCHLIST,
            MediaItem.STATUS_WATCHING,
        )
        val HISTORY_STATUSES = listOf(MediaItem.STATUS_WATCHED)
        val WhileSubscribed = SharingStarted.WhileSubscribed(5_000)
    }
}
