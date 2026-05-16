package com.filmo

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import dagger.hilt.android.HiltAndroidApp
import com.filmo.ui.movie.newMoviePosterImageLoader
import timber.log.Timber

@HiltAndroidApp
class FilmoApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun newImageLoader(context: Context): ImageLoader {
        return newMoviePosterImageLoader(context)
    }
}
