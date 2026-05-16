package com.filmo.ui.movie

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoviePosterImageCacheTest {
    @Test
    fun posterImageCacheConfigUsesReusableMemoryAndDiskCache() {
        assertEquals("movie_poster_image_cache", moviePosterDiskCacheDirectoryName())
        assertTrue(moviePosterMemoryCacheMaxSizePercent() > 0.0)
        assertTrue(moviePosterDiskCacheMaxSizePercent() > 0.0)
    }
}
