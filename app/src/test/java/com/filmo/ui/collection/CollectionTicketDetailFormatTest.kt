package com.filmo.ui.collection

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
}
