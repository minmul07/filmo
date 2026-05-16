package com.filmo.ui.movie

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import okio.Path.Companion.toOkioPath

internal fun newMoviePosterImageLoader(context: Context): ImageLoader {
    return ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, moviePosterMemoryCacheMaxSizePercent())
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve(moviePosterDiskCacheDirectoryName()).toOkioPath())
                .maxSizePercent(moviePosterDiskCacheMaxSizePercent())
                .build()
        }
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .build()
}

internal fun moviePosterDiskCacheDirectoryName(): String = "movie_poster_image_cache"

internal fun moviePosterMemoryCacheMaxSizePercent(): Double = 0.25

internal fun moviePosterDiskCacheMaxSizePercent(): Double = 0.02
