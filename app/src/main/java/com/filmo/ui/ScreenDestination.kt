package com.filmo.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ScreenDestination : NavKey {
    @Serializable
    data object TicketView : ScreenDestination

    @Serializable
    data object Record : ScreenDestination

    @Serializable
    data object RecordViewingInfo : ScreenDestination

    @Serializable
    data object Collection : ScreenDestination

    @Serializable
    data class CollectionDetail(val ticketId: String) : ScreenDestination

    @Serializable
    data class CollectionEdit(val ticketId: String) : ScreenDestination
}
