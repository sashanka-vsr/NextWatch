package com.nextwatch.app.data

import android.content.Context

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

    companion object {
        const val ROUTE_HOME = "hub"
        const val ROUTE_WATCHLIST = "watchlist"
        const val ROUTE_HISTORY = "history"

        private const val PREFS_NAME = "nextwatch_prefs"
        private const val KEY_LANDING = "landing_page"
    }
}
