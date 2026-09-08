package com.nextwatch.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nextwatch.app.data.DatabaseProvider

class ViewModelFactory(
    context: Context,
) : ViewModelProvider.Factory {

    private val mediaDao = DatabaseProvider.mediaDao(context.applicationContext)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NextWatchViewModel::class.java)) {
            return NextWatchViewModel(mediaDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
