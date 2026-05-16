package com.filmo.ui.collection

import androidx.lifecycle.ViewModel
import com.filmo.service.AppRepository
import com.filmo.service.MovieTicket
import com.filmo.service.UpdateTicketRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    suspend fun loadTickets() {
        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        val result = appRepository.fetchTicketCollection()
        _uiState.update { state ->
            result.fold(
                onSuccess = { collection ->
                    state.copy(
                        myTickets = collection.myTickets,
                        savedTickets = collection.savedTickets,
                        isLoading = false,
                        errorMessage = null
                    )
                },
                onFailure = {
                    state.copy(
                        isLoading = false,
                        errorMessage = "티켓 컬렉션을 불러오지 못했어요. 다시 시도해 주세요."
                    )
                }
            )
        }
    }

    fun selectTab(tab: CollectionTicketTab) {
        _uiState.update {
            it.copy(
                selectedTab = tab,
                pendingDeleteTicket = null,
                errorMessage = null
            )
        }
    }

    fun requestDeleteTicket(ticketId: String) {
        _uiState.update { state ->
            state.copy(
                pendingDeleteTicket = state.findTicket(ticketId),
                errorMessage = null
            )
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(pendingDeleteTicket = null) }
    }

    suspend fun confirmDeleteTicket() {
        val ticket = _uiState.value.pendingDeleteTicket ?: return
        val result = if (ticket.ownedByMe) {
            appRepository.deleteMyTicket(ticket.id)
        } else {
            appRepository.removeSavedTicket(ticket.id)
        }

        _uiState.update { state ->
            result.fold(
                onSuccess = {
                    state.copy(
                        myTickets = state.myTickets.filterNot { it.id == ticket.id },
                        savedTickets = state.savedTickets.filterNot { it.id == ticket.id },
                        pendingDeleteTicket = null,
                        errorMessage = null
                    )
                },
                onFailure = {
                    state.copy(
                        pendingDeleteTicket = null,
                        errorMessage = "티켓을 삭제하지 못했어요. 다시 시도해 주세요."
                    )
                }
            )
        }
    }

    fun startEditingTicket(ticketId: String) {
        _uiState.update { state ->
            val ticket = state.myTickets.firstOrNull { it.id == ticketId }
            state.copy(
                editingTicket = ticket?.toEditingTicket(),
                errorMessage = if (ticket == null) "수정할 티켓을 찾지 못했어요." else null
            )
        }
    }

    fun cancelEditingTicket() {
        _uiState.update { it.copy(editingTicket = null, errorMessage = null) }
    }

    fun updateEditingTheaterName(theaterName: String) {
        _uiState.update { state ->
            state.copy(
                editingTicket = state.editingTicket?.copy(theaterName = theaterName),
                errorMessage = null
            )
        }
    }

    fun updateEditingWatchedDate(watchedDate: String) {
        _uiState.update { state ->
            state.copy(
                editingTicket = state.editingTicket?.copy(watchedDate = watchedDate),
                errorMessage = null
            )
        }
    }

    fun updateEditingRating(rating: Int) {
        _uiState.update { state ->
            state.copy(
                editingTicket = state.editingTicket?.copy(rating = rating.coerceIn(MIN_RATING, MAX_RATING)),
                errorMessage = null
            )
        }
    }

    fun updateEditingReview(review: String) {
        _uiState.update { state ->
            state.copy(
                editingTicket = state.editingTicket?.copy(review = review),
                errorMessage = null
            )
        }
    }

    suspend fun saveEditedTicket() {
        val editingTicket = _uiState.value.editingTicket ?: return
        if (editingTicket.theaterName.isBlank() ||
            editingTicket.watchedDate.isBlank() ||
            editingTicket.review.isBlank()
        ) {
            _uiState.update {
                it.copy(errorMessage = "영화관, 관람일, 관람 후기를 입력해 주세요.")
            }
            return
        }

        val result = appRepository.updateMyTicket(
            UpdateTicketRequest(
                ticketId = editingTicket.id,
                theaterName = editingTicket.theaterName,
                watchedDate = editingTicket.watchedDate,
                rating = editingTicket.rating,
                review = editingTicket.review
            )
        )

        _uiState.update { state ->
            result.fold(
                onSuccess = { updatedTicket ->
                    state.copy(
                        myTickets = state.myTickets.map {
                            if (it.id == updatedTicket.id) updatedTicket else it
                        },
                        editingTicket = null,
                        errorMessage = null
                    )
                },
                onFailure = {
                    state.copy(errorMessage = "티켓을 저장하지 못했어요. 다시 시도해 주세요.")
                }
            )
        }
    }

    private companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5
    }
}

data class CollectionUiState(
    val selectedTab: CollectionTicketTab = CollectionTicketTab.MyTickets,
    val myTickets: List<MovieTicket> = emptyList(),
    val savedTickets: List<MovieTicket> = emptyList(),
    val isLoading: Boolean = false,
    val pendingDeleteTicket: MovieTicket? = null,
    val editingTicket: EditingTicket? = null,
    val errorMessage: String? = null
) {
    val visibleTickets: List<MovieTicket>
        get() = when (selectedTab) {
            CollectionTicketTab.MyTickets -> myTickets
            CollectionTicketTab.SavedTickets -> savedTickets
        }

    fun findTicket(ticketId: String): MovieTicket? {
        return myTickets.firstOrNull { it.id == ticketId }
            ?: savedTickets.firstOrNull { it.id == ticketId }
    }
}

enum class CollectionTicketTab(
    val label: String
) {
    MyTickets("내 티켓"),
    SavedTickets("저장한 티켓")
}

data class EditingTicket(
    val id: String,
    val movieTitle: String,
    val theaterName: String,
    val watchedDate: String,
    val rating: Int,
    val review: String
)

private fun MovieTicket.toEditingTicket(): EditingTicket {
    return EditingTicket(
        id = id,
        movieTitle = movieTitle,
        theaterName = theaterName,
        watchedDate = watchedDate,
        rating = rating,
        review = review
    )
}
