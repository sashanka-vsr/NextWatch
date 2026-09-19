package com.nextwatch.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nextwatch.app.data.AppPreferences
import com.nextwatch.app.data.DatabaseProvider
import com.nextwatch.app.data.PosterCache
import com.nextwatch.app.network.NextWatchApiClient

class ViewModelFactory(
    context: Context,
) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext
    private val mediaDao = DatabaseProvider.mediaDao(appContext)
    private val mediaGenreDao = DatabaseProvider.mediaGenreDao(appContext)
    private val watchProviderDao = DatabaseProvider.watchProviderDao(appContext)
    private val posterCache = PosterCache(appContext)
    private val preferences = AppPreferences(appContext)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NextWatchViewModel::class.java)) {
            return NextWatchViewModel(
                mediaDao = mediaDao,
                mediaGenreDao = mediaGenreDao,
                watchProviderDao = watchProviderDao,
                posterCache = posterCache,
                apiClient = NextWatchApiClient(
                    omdbApiKeyProvider = { preferences.getEffectiveOmdbApiKey() },
                ),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}