package com.filmo.ui.collection

import com.filmo.service.MovieTicket
import org.junit.Assert.assertEquals
import org.junit.Test

class CollectionTicketDetailFormatTest {
    @Test
    fun watchedDateUsesKoreanDetailFormatWhenIsoDateIsValid() {
        assertEquals(
            "2026년 05월 16일 (토)",
            "2026-05-16".toCollectionDetailWatchedDateText()
        )
    }

    @Test
    fun watchedDateFallsBackToOriginalTextWhenDateIsInvalid() {
        assertEquals(
            "2026.05.16",
            "2026.05.16".toCollectionDetailWatchedDateText()
        )
    }

    @Test
    fun watchedDateShowsEmptyStateWhenBlank() {
        assertEquals(
            "관람일 미입력",
            "".toCollectionDetailWatchedDateText()
        )
    }

    @Test
    fun movieMetadataUsesAvailableMovieFieldsWithoutTheaterFallback() {
        val ticket = MovieTicket(
            id = "ticket-1",
            movieTitle = "스틸 플라워",
            theaterName = "",
            watchedDate = "2026-05-16",
            rating = 3,
            review = "좋았어요",
            ownedByMe = true,
            savedByMe = false,
            genre = "드라마",
            director = "박석영",
            releaseYear = 2016,
            duration = "83분"
        )

        assertEquals(
            "드라마 · 박석영 감독 · 2016년 · 83분",
            ticket.toCollectionDetailMovieMetadataText()
        )
    }

    @Test
    fun movieMetadataFallsBackToIndependentFilmWhenMovieFieldsAreMissing() {
        val ticket = MovieTicket(
            id = "ticket-1",
            movieTitle = "스틸 플라워",
            theaterName = "",
            watchedDate = "2026-05-16",
            rating = 3,
            review = "좋았어요",
            ownedByMe = true,
            savedByMe = false
        )

        assertEquals(
            "독립영화",
            ticket.toCollectionDetailMovieMetadataText()
        )
    }
}
