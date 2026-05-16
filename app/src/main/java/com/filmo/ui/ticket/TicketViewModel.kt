package com.filmo.ui.ticket

import androidx.lifecycle.ViewModel
import com.filmo.service.AppRepository
import com.filmo.service.PublicTicket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TicketViewUiState())
    val uiState: StateFlow<TicketViewUiState> = _uiState.asStateFlow()

    suspend fun loadPublicTickets() {
        if (_uiState.value.isLoading) {
            Timber.d("TicketViewModel.loadPublicTickets ignored: already loading")
            return
        }

        Timber.d("TicketViewModel.loadPublicTickets request")
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        val result = appRepository.fetchPublicTickets()
        result
            .onSuccess { tickets ->
                Timber.d("TicketViewModel.loadPublicTickets success count=%d", tickets.size)
            }
            .onFailure {
                Timber.w(it, "TicketViewModel.loadPublicTickets failed")
            }

        _uiState.update { state ->
            result.fold(
                onSuccess = { tickets ->
                    state.copy(
                        tickets = tickets,
                        isLoading = false,
                        errorMessage = null
                    )
                },
                onFailure = {
                    state.copy(
                        isLoading = false,
                        errorMessage = "공개 티켓을 불러오지 못했어요. 다시 시도해 주세요."
                    )
                }
            )
        }
    }

    suspend fun toggleTicketLike(ticketId: String) {
        val ticket = _uiState.value.tickets.firstOrNull { it.id == ticketId }
        if (ticket == null) {
            Timber.d("TicketViewModel.toggleTicketLike ticket not found ticketId=%s", ticketId)
            return
        }

        val nextLiked = !ticket.liked
        Timber.d("TicketViewModel.toggleTicketLike request ticketId=%s liked=%s", ticketId, nextLiked)
        _uiState.update { state ->
            state.copy(
                tickets = state.tickets.map { current ->
                    if (current.id == ticketId) current.withLikeState(nextLiked) else current
                },
                errorMessage = null
            )
        }

        val result = appRepository.setTicketLiked(ticketId = ticketId, liked = nextLiked)
        result
            .onSuccess {
                Timber.d("TicketViewModel.toggleTicketLike success ticketId=%s liked=%s", ticketId, nextLiked)
            }
            .onFailure {
                Timber.w(it, "TicketViewModel.toggleTicketLike failed ticketId=%s liked=%s", ticketId, nextLiked)
            }

        if (result.isFailure) {
            _uiState.update { state ->
                state.copy(
                    tickets = state.tickets.map { current ->
                        if (current.id == ticketId) ticket else current
                    },
                    errorMessage = "좋아요를 변경하지 못했어요. 다시 시도해 주세요."
                )
            }
        }
    }
}

data class TicketViewUiState(
    val tickets: List<PublicTicket> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

private fun PublicTicket.withLikeState(liked: Boolean): PublicTicket {
    val delta = when {
        this.liked == liked -> 0
        liked -> 1
        else -> -1
    }
    return copy(
        liked = liked,
        likeCount = (likeCount + delta).coerceAtLeast(0)
    )
}
