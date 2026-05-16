package com.filmo.ui.movie

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun Long?.toWatchedYearText(): String {
    return this?.let { millis ->
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("yyyy년", Locale.KOREAN))
    }.orEmpty()
}

internal fun Long?.toWatchedDateText(): String {
    return this?.let { millis ->
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("MM월 dd일(E)", Locale.KOREAN))
    }.orEmpty()
}

internal fun Long?.toWatchedDateWithYearText(): String {
    return this?.let { millis ->
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (E)", Locale.KOREAN))
    }.orEmpty()
}
