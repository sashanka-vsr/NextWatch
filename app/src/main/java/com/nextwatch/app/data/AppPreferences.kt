package com.nextwatch.app.data

import android.content.Context
import com.nextwatch.app.network.ApiKeys

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

    companion object {
        const val ROUTE_HOME = "hub"
        const val ROUTE_WATCHLIST = "watchlist"
        const val ROUTE_HISTORY = "history"

        private const val PREFS_NAME = "nextwatch_prefs"
        private const val KEY_LANDING = "landing_page"
        private const val KEY_OMDB_API_KEY = "omdb_api_key"
        private const val KEY_WATCH_REGION = "watch_region"
    }
}
