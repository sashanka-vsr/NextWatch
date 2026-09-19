package com.nextwatch.app.data

import android.content.Context
import com.nextwatch.app.network.ApiKeys
import com.nextwatch.app.ui.screens.SortDirection
import com.nextwatch.app.ui.screens.WatchlistSort
import com.nextwatch.app.ui.screens.WatchlistSortCriterion

class AppPreferences(
    context: Context,
) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLandingRoute(): String {
        return when (val stored = prefs.getString(KEY_LANDING, ROUTE_HOME)) {
            ROUTE_WATCHLIST, ROUTE_HISTORY -> stored
            else -> ROUTE_HOME
        }
    }

    fun setLandingRoute(route: String) {
        val value = when (route) {
            ROUTE_WATCHLIST, ROUTE_HISTORY -> route
            else -> ROUTE_HOME
        }
        prefs.edit().putString(KEY_LANDING, value).apply()
    }

    fun getOmdbApiKey(): String {
        return prefs.getString(KEY_OMDB_API_KEY, "").orEmpty()
    }

    fun setOmdbApiKey(key: String) {
        prefs.edit().putString(KEY_OMDB_API_KEY, key.trim()).apply()
    }

    fun getEffectiveOmdbApiKey(): String {
        val customKey = getOmdbApiKey().trim()
        return if (customKey.isNotEmpty()) customKey else ApiKeys.OMDB
    }

    fun getWatchRegion(): String {
        val stored = prefs.getString(KEY_WATCH_REGION, null)
        if (!stored.isNullOrBlank()) return stored
        return java.util.Locale.getDefault().country.ifBlank { "US" }
    }

    fun setWatchRegion(region: String) {
        prefs.edit().putString(KEY_WATCH_REGION, region).apply()
    }

    // ── Sorting Preferences ───────────────────────────────────────────────────

    fun getWatchlistMovieSort(): WatchlistSort =
        getSort(KEY_WATCHLIST_MOVIE_SORT_CRITERION, KEY_WATCHLIST_MOVIE_SORT_DIRECTION)

    fun setWatchlistMovieSort(sort: WatchlistSort) =
        setSort(KEY_WATCHLIST_MOVIE_SORT_CRITERION, KEY_WATCHLIST_MOVIE_SORT_DIRECTION, sort)

    fun getWatchlistSeriesSort(): WatchlistSort =
        getSort(KEY_WATCHLIST_SERIES_SORT_CRITERION, KEY_WATCHLIST_SERIES_SORT_DIRECTION)

    fun setWatchlistSeriesSort(sort: WatchlistSort) =
        setSort(KEY_WATCHLIST_SERIES_SORT_CRITERION, KEY_WATCHLIST_SERIES_SORT_DIRECTION, sort)

    fun getHistoryMovieSort(): WatchlistSort =
        getSort(KEY_HISTORY_MOVIE_SORT_CRITERION, KEY_HISTORY_MOVIE_SORT_DIRECTION)

    fun setHistoryMovieSort(sort: WatchlistSort) =
        setSort(KEY_HISTORY_MOVIE_SORT_CRITERION, KEY_HISTORY_MOVIE_SORT_DIRECTION, sort)

    fun getHistorySeriesSort(): WatchlistSort =
        getSort(KEY_HISTORY_SERIES_SORT_CRITERION, KEY_HISTORY_SERIES_SORT_DIRECTION)

    fun setHistorySeriesSort(sort: WatchlistSort) =
        setSort(KEY_HISTORY_SERIES_SORT_CRITERION, KEY_HISTORY_SERIES_SORT_DIRECTION, sort)

    private fun getSort(criterionKey: String, directionKey: String): WatchlistSort {
        val criterionName = prefs.getString(criterionKey, null)
        val directionName = prefs.getString(directionKey, null)

        val criterion = criterionName?.let { name ->
            try { WatchlistSortCriterion.valueOf(name) } catch (_: Exception) { null }
        } ?: WatchlistSortCriterion.Added

        val direction = directionName?.let { name ->
            try { SortDirection.valueOf(name) } catch (_: Exception) { null }
        } ?: SortDirection.Descending

        return WatchlistSort(criterion, direction)
    }

    private fun setSort(criterionKey: String, directionKey: String, sort: WatchlistSort) {
        prefs.edit()
            .putString(criterionKey, sort.criterion.name)
            .putString(directionKey, sort.direction.name)
            .apply()
    }

    companion object {
        const val ROUTE_HOME = "hub"
        const val ROUTE_WATCHLIST = "watchlist"
        const val ROUTE_HISTORY = "history"

        private const val PREFS_NAME = "nextwatch_prefs"
        private const val KEY_LANDING = "landing_page"
        private const val KEY_OMDB_API_KEY = "omdb_api_key"
        private const val KEY_WATCH_REGION = "watch_region"

        private const val KEY_WATCHLIST_MOVIE_SORT_CRITERION = "watchlist_movie_sort_criterion"
        private const val KEY_WATCHLIST_MOVIE_SORT_DIRECTION = "watchlist_movie_sort_direction"
        private const val KEY_WATCHLIST_SERIES_SORT_CRITERION = "watchlist_series_sort_criterion"
        private const val KEY_WATCHLIST_SERIES_SORT_DIRECTION = "watchlist_series_sort_direction"
        private const val KEY_HISTORY_MOVIE_SORT_CRITERION = "history_movie_sort_criterion"
        private const val KEY_HISTORY_MOVIE_SORT_DIRECTION = "history_movie_sort_direction"
        private const val KEY_HISTORY_SERIES_SORT_CRITERION = "history_series_sort_criterion"
        private const val KEY_HISTORY_SERIES_SORT_DIRECTION = "history_series_sort_direction"
    }
}
