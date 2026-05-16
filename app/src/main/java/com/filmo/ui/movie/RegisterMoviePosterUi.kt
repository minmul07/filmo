package com.filmo.ui.movie

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.filmo.BuildConfig
import com.filmo.service.MovieCatalogItem
import timber.log.Timber

@Composable
internal fun MoviePoster(
    movie: MovieCatalogItem,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(10.dp),
    showBorder: Boolean = true
) {
    MoviePosterImage(
        imageUrl = remember(movie.imagePath) { movie.imageUrl() },
        title = movie.title,
        modifier = modifier,
        shape = shape,
        showBorder = showBorder
    )
}

@Composable
internal fun MoviePosterImage(
    imageUrl: String,
    title: String,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(10.dp),
    showBorder: Boolean = true
) {
    val context = LocalPlatformContext.current
    var showFallback by remember(imageUrl) {
        mutableStateOf(imageUrl.isBlank())
    }
    val imageRequest: ImageRequest = remember(context, imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl.takeIf { it.isNotBlank() })
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    LaunchedEffect(imageUrl) {
        if (imageUrl.isBlank()) {
            Timber.d("MoviePosterImage.loadImage skipped reason=blank_url titleLength=%d", title.length)
        } else {
            Timber.d(
                "MoviePosterImage.loadImage request urlType=%s urlLength=%d titleLength=%d",
                imageUrl.urlTypeForLog(),
                imageUrl.length,
                title.length
            )
        }
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
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "$title 포스터",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = {
                        showFallback = true
                    },
                    onSuccess = {
                        showFallback = false
                        Timber.d(
                            "MoviePosterImage.loadImage success urlType=%s dataSource=%s",
                            imageUrl.urlTypeForLog(),
                            it.result.dataSource
                        )
                    },
                    onError = {
                        showFallback = true
                        Timber.w(
                            "MoviePosterImage.loadImage failed urlType=%s urlLength=%d",
                            imageUrl.urlTypeForLog(),
                            imageUrl.length
                        )
                    }
                )
            }

            if (showFallback) {
                Icon(
                    imageVector = Icons.Filled.Movie,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
