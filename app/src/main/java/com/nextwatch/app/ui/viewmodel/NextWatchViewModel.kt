package com.nextwatch.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextwatch.app.data.MediaDao
import com.nextwatch.app.data.MediaGenre
import com.nextwatch.app.data.MediaGenreDao
import com.nextwatch.app.data.MediaItem
import com.nextwatch.app.network.NextWatchApiClient
import com.nextwatch.app.network.OmdbDetails
import com.nextwatch.app.network.TmdbMovie
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.nextwatch.app.data.PosterCache
import kotlinx.coroutines.flow.Flow

class NextWatchViewModel(
    private val mediaDao: MediaDao,
    private val mediaGenreDao: MediaGenreDao,
    private val posterCache: PosterCache,
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
    
    fun observeMedia(id: Long): Flow<MediaItem?> {
        return mediaDao.observeById(id)
    }

    fun observeGenres(mediaId: Long): Flow<List<String>> {
        return mediaGenreDao.observeGenres(mediaId)
    }

    fun addMediaFromSearchResult(
    media: TmdbMovie,
    status: String,
    onComplete: () -> Unit,
) {
        viewModelScope.launch {
            runCatching {
                val tmdbDetails = apiClient.fetchTmdbDetails(
                    tmdbId = media.tmdbId,
                    mediaType = media.mediaType,
                ) ?: return@launch

                val omdbDetails = tmdbDetails.imdbId?.let { imdbId ->
                    runCatching {
                        apiClient.fetchOmdbDetails(imdbId)
                    }.getOrNull()
                }

                val posterLocalPath = posterCache.downloadPoster(
                    posterUrl = tmdbDetails.posterUrl,
                    tmdbId = tmdbDetails.tmdbId,
                    imdbId = tmdbDetails.imdbId,
                )

                val mediaId = mediaDao.upsert(
                    MediaItem(
                        tmdbId = tmdbDetails.tmdbId,
                        imdbId = tmdbDetails.imdbId,

                        title = tmdbDetails.title,
                        type = media.mediaType,
                        status = status,

                        overview = tmdbDetails.overview,

                        posterUrl = tmdbDetails.posterUrl,
                        posterLocalPath = posterLocalPath,

                        releaseDate = tmdbDetails.releaseDate,
                        runtimeMinutes = tmdbDetails.runtimeMinutes,
                        seasonCount = tmdbDetails.seasonCount,

                        imdbRating = omdbDetails?.imdbRating,
                    ),
                )

                mediaGenreDao.deleteForMedia(mediaId)

                mediaGenreDao.insertAll(
                    tmdbDetails.genres.map { genre ->
                        MediaGenre(
                            mediaId = mediaId,
                            genre = genre,
                        )
                    },
                )
            }

            onComplete()
        }
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
