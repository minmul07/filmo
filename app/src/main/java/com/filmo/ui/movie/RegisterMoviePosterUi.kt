package com.filmo.ui.movie

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.filmo.BuildConfig
import com.filmo.service.MovieCatalogItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.URL

@Composable
internal fun MoviePoster(
    movie: MovieCatalogItem,
    imageCache: MoviePosterBitmapSessionCache,
    modifier: Modifier = Modifier
) {
    MoviePosterImage(
        imageUrl = remember(movie.imagePath) { movie.imageUrl() },
        title = movie.title,
        imageCache = imageCache,
        modifier = modifier
    )
}

@Composable
internal fun MoviePosterImage(
    imageUrl: String,
    title: String,
    imageCache: MoviePosterBitmapSessionCache,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(10.dp),
    showBorder: Boolean = true
) {
    val bitmap by produceState<ImageBitmap?>(
        initialValue = imageCache[imageUrl],
        imageUrl,
        imageCache
    ) {
        value = imageCache[imageUrl]
        if (value != null) {
            Timber.d("MoviePosterImage.loadImage memory_cache_hit urlType=%s", imageUrl.urlTypeForLog())
            return@produceState
        }
        if (imageUrl.isBlank()) {
            Timber.d("MoviePosterImage.loadImage skipped reason=blank_url titleLength=%d", title.length)
            return@produceState
        }

        Timber.d(
            "MoviePosterImage.loadImage request urlType=%s urlLength=%d titleLength=%d",
            imageUrl.urlTypeForLog(),
            imageUrl.length,
            title.length
        )

        val loadedBitmap = withContext(Dispatchers.IO) {
            runCatching {
                URL(imageUrl).openStream().use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
                }
            }.onSuccess { imageBitmap ->
                Timber.d(
                    "MoviePosterImage.loadImage success urlType=%s decoded=%s",
                    imageUrl.urlTypeForLog(),
                    imageBitmap != null
                )
            }.onFailure {
                Timber.w(
                    it,
                    "MoviePosterImage.loadImage failed urlType=%s urlLength=%d",
                    imageUrl.urlTypeForLog(),
                    imageUrl.length
                )
            }.getOrNull()
        }
        if (loadedBitmap != null) {
            imageCache[imageUrl] = loadedBitmap
        }
        value = loadedBitmap
    }

    Surface(
        modifier = modifier.clip(shape),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = if (showBorder) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        } else {
            null
        }
    ) {
        if (bitmap == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Movie,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Image(
                bitmap = bitmap!!,
                contentDescription = "$title 포스터",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
internal fun rememberMoviePosterBitmapSessionCache(): MoviePosterBitmapSessionCache {
    return remember {
        MoviePosterBitmapSessionCache()
    }
}

@Stable
internal class MoviePosterBitmapSessionCache {
    private val cache = mutableMapOf<String, ImageBitmap>()

    operator fun get(imageUrl: String): ImageBitmap? {
        return if (imageUrl.isBlank()) {
            null
        } else {
            cache.get(imageUrl)
        }
    }

    operator fun set(imageUrl: String, bitmap: ImageBitmap) {
        if (imageUrl.isNotBlank()) {
            cache.put(imageUrl, bitmap)
        }
    }
}

internal val MovieCatalogItem.listKey: String
    get() = id.ifBlank { "$title-$releaseYear-$director" }

internal fun MovieCatalogItem.imageUrl(): String {
    return imagePath.toMovieImageUrl()
}

internal fun String.toMovieImageUrl(): String {
    val path = trim()
    if (path.isBlank()) {
        Timber.d("MovieImageUrl.resolve skipped reason=blank_path")
        return ""
    }
    if (path.startsWith("http://") || path.startsWith("https://")) {
        Timber.d("MovieImageUrl.resolve absolute pathLength=%d", path.length)
        return path
    }

    Timber.d("MovieImageUrl.resolve api pathLength=%d", path.length)
    return "${BuildConfig.API_BASE_URL.trimEnd('/')}/api/movies/image/${path.trimStart('/')}"
}

private fun String.urlTypeForLog(): String {
    return when {
        startsWith("https://") -> "https"
        startsWith("http://") -> "http"
        else -> "relative_or_unknown"
    }
}
