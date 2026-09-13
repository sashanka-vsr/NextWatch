package com.nextwatch.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nextwatch.app.data.DatabaseProvider
import com.nextwatch.app.data.PosterCache

class ViewModelFactory(
    context: Context,
) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext
    private val mediaDao = DatabaseProvider.mediaDao(appContext)
    private val mediaGenreDao = DatabaseProvider.mediaGenreDao(appContext)
    private val posterCache = PosterCache(appContext)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NextWatchViewModel::class.java)) {
            return NextWatchViewModel(
                mediaDao = mediaDao,
                mediaGenreDao = mediaGenreDao,
                posterCache = posterCache,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}