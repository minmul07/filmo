package com.filmo.ui.collection

import androidx.lifecycle.ViewModel
import com.filmo.service.AppRepository
import com.filmo.service.MovieTicket
import com.filmo.service.UpdateTicketRequest
import com.filmo.ui.common.toTicketLikeErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    suspend fun loadTickets() {
        if (_uiState.value.isLoading) {
            Timber.d("CollectionViewModel.loadTickets ignored: already loading")
            return
        }

        Timber.d("CollectionViewModel.loadTickets request")
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        val result = appRepository.fetchTicketCollection()
        result
            .onSuccess { collection ->
                Timber.d(
                    "CollectionViewModel.loadTickets success myCount=%d savedCount=%d",
                    collection.myTickets.size,
                    collection.savedTickets.size
                )
            }
            .onFailure {
                Timber.w(it, "CollectionViewModel.loadTickets failed")
            }
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
        Timber.d("CollectionViewModel.selectTab tab=%s", tab.name)
        _uiState.update {
            it.copy(
                selectedTab = tab,
                pendingDeleteTicket = null,
                errorMessage = null
            )
        }
    }

    fun requestDeleteTicket(ticketId: String) {
        Timber.d("CollectionViewModel.requestDeleteTicket ticketId=%s", ticketId)
        _uiState.update { state ->
            state.copy(
                pendingDeleteTicket = state.findTicket(ticketId),
                errorMessage = null
            )
        }
    }

    fun dismissDeleteDialog() {
        Timber.d("CollectionViewModel.dismissDeleteDialog")
        _uiState.update { it.copy(pendingDeleteTicket = null) }
    }

    suspend fun confirmDeleteTicket() {
        val ticket = _uiState.value.pendingDeleteTicket ?: return
        Timber.d(
            "CollectionViewModel.confirmDeleteTicket request ticketId=%s ownedByMe=%s",
            ticket.id,
            ticket.ownedByMe
        )
        val result = if (ticket.ownedByMe) {
            appRepository.deleteMyTicket(ticket.id)
        } else {
            appRepository.removeSavedTicket(ticket.id)
        }
        result
            .onSuccess {
                Timber.d("CollectionViewModel.confirmDeleteTicket success ticketId=%s", ticket.id)
            }
            .onFailure {
                Timber.w(it, "CollectionViewModel.confirmDeleteTicket failed ticketId=%s", ticket.id)
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

    suspend fun loadTicketDetail(ticketId: String) {
        if (_uiState.value.myTickets.isEmpty() && _uiState.value.savedTickets.isEmpty()) {
            loadTickets()
        }

        val ticket = _uiState.value.findTicket(ticketId)
        if (ticket == null) {
            Timber.d("CollectionViewModel.loadTicketDetail ticket not found ticketId=%s", ticketId)
            _uiState.update {
                it.copy(errorMessage = "티켓을 찾지 못했어요.")
            }
            return
        }

        Timber.d(
            "CollectionViewModel.loadTicketDetail request ticketId=%s ownedByMe=%s",
            ticket.id,
            ticket.ownedByMe
        )
        val result = appRepository.fetchTicketDetail(
            ticketId = ticket.id,
            ownedByMe = ticket.ownedByMe
        )
        result
            .onSuccess { detail ->
                Timber.d(
                    "CollectionViewModel.loadTicketDetail success ticketId=%s liked=%s likeCount=%d",
                    detail.id,
                    detail.liked,
                    detail.likeCount
                )
            }
            .onFailure {
                Timber.w(it, "CollectionViewModel.loadTicketDetail failed ticketId=%s", ticket.id)
            }

        _uiState.update { state ->
            result.fold(
                onSuccess = { detail ->
                    state.copy(
                        myTickets = state.myTickets.replaceTicket(ticket.id, detail),
                        savedTickets = state.savedTickets.replaceTicket(ticket.id, detail),
                        errorMessage = null
                    )
                },
                onFailure = {
                    state.copy(errorMessage = "티켓 상세 정보를 불러오지 못했어요. 다시 시도해 주세요.")
                }
            )
        }
    }

    suspend fun toggleTicketLike(ticketId: String) {
        val ticket = _uiState.value.findTicket(ticketId)
        if (ticket == null) {
            Timber.d("CollectionViewModel.toggleTicketLike ticket not found ticketId=%s", ticketId)
            return
        }

        val nextLiked = !ticket.liked
        Timber.d("CollectionViewModel.toggleTicketLike request ticketId=%s liked=%s", ticketId, nextLiked)
        _uiState.update { state ->
            state.copy(
                myTickets = state.myTickets.updateLike(ticketId, nextLiked),
                savedTickets = state.savedTickets.updateLike(ticketId, nextLiked),
                errorMessage = null
            )
        }

        val result = appRepository.setTicketLiked(ticketId = ticketId, liked = nextLiked)
        result
            .onSuccess {
                Timber.d("CollectionViewModel.toggleTicketLike success ticketId=%s liked=%s", ticketId, nextLiked)
            }
            .onFailure {
                Timber.w(it, "CollectionViewModel.toggleTicketLike failed ticketId=%s liked=%s", ticketId, nextLiked)
            }

        result.exceptionOrNull()?.let { error ->
            _uiState.update { state ->
                state.copy(
                    myTickets = state.myTickets.restoreTicket(ticket),
                    savedTickets = state.savedTickets.restoreTicket(ticket),
                    errorMessage = error.toTicketLikeErrorMessage()
                )
            }
        }
    }

    fun startEditingTicket(ticketId: String) {
        Timber.d("CollectionViewModel.startEditingTicket ticketId=%s", ticketId)
        _uiState.update { state ->
            val ticket = state.myTickets.firstOrNull { it.id == ticketId }
            if (ticket == null) {
                Timber.d("CollectionViewModel.startEditingTicket ticket not found ticketId=%s", ticketId)
            }
            state.copy(
                editingTicket = ticket?.toEditingTicket(),
                errorMessage = if (ticket == null) "수정할 티켓을 찾지 못했어요." else null
            )
        }
    }

    fun cancelEditingTicket() {
        Timber.d("CollectionViewModel.cancelEditingTicket")
        _uiState.update { it.copy(editingTicket = null, errorMessage = null) }
    }

    fun updateEditingTheaterName(theaterName: String) {
        Timber.d(
            "CollectionViewModel.updateEditingTheaterName input length=%d blank=%s",
            theaterName.length,
            theaterName.isBlank()
        )
        _uiState.update { state ->
            state.copy(
                editingTicket = state.editingTicket?.copy(theaterName = theaterName),
                errorMessage = null
            )
        }
    }

    fun updateEditingWatchedDate(watchedDate: String) {
        Timber.d(
            "CollectionViewModel.updateEditingWatchedDate input length=%d blank=%s",
            watchedDate.length,
            watchedDate.isBlank()
        )
        _uiState.update { state ->
            state.copy(
                editingTicket = state.editingTicket?.copy(watchedDate = watchedDate),
                errorMessage = null
            )
        }
    }

    fun updateEditingRating(rating: Int) {
        Timber.d("CollectionViewModel.updateEditingRating input=%d coerced=%d", rating, rating.coerceIn(MIN_RATING, MAX_RATING))
        _uiState.update { state ->
            state.copy(
                editingTicket = state.editingTicket?.copy(rating = rating.coerceIn(MIN_RATING, MAX_RATING)),
                errorMessage = null
            )
        }
    }

    fun updateEditingReview(review: String) {
        Timber.d("CollectionViewModel.updateEditingReview input length=%d", review.length)
        _uiState.update { state ->
            state.copy(
                editingTicket = state.editingTicket?.copy(review = review),
                errorMessage = null
            )
        }
    }

    suspend fun saveEditedTicket() {
        val editingTicket = _uiState.value.editingTicket ?: return
        Timber.d("CollectionViewModel.saveEditedTicket request ticketId=%s", editingTicket.id)
        if (editingTicket.watchedDate.isBlank() ||
            editingTicket.review.isBlank()
        ) {
            Timber.d(
                "CollectionViewModel.saveEditedTicket blocked watchedDateBlank=%s reviewBlank=%s",
                editingTicket.watchedDate.isBlank(),
                editingTicket.review.isBlank()
            )
            _uiState.update {
                it.copy(errorMessage = "관람일, 관람 후기를 입력해 주세요.")
            }
            return
        }

        val result = appRepository.updateMyTicket(
            UpdateTicketRequest(
                ticketId = editingTicket.id,
                watchedDate = editingTicket.watchedDate,
                watchedTime = DefaultWatchedTime,
                rating = editingTicket.rating,
                review = editingTicket.review
            )
        )
        result
            .onSuccess { updatedTicket ->
                Timber.d("CollectionViewModel.saveEditedTicket success ticketId=%s", updatedTicket.id)
            }
            .onFailure {
                Timber.w(it, "CollectionViewModel.saveEditedTicket failed ticketId=%s", editingTicket.id)
            }

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
        const val DefaultWatchedTime = "00:00"
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

private fun List<MovieTicket>.updateLike(ticketId: String, liked: Boolean): List<MovieTicket> {
    return map { ticket ->
        if (ticket.id == ticketId) ticket.withLikeState(liked) else ticket
    }
}

private fun List<MovieTicket>.restoreTicket(ticket: MovieTicket): List<MovieTicket> {
    return map { current ->
        if (current.id == ticket.id) ticket else current
    }
}

private fun List<MovieTicket>.replaceTicket(ticketId: String, updatedTicket: MovieTicket): List<MovieTicket> {
    return map { current ->
        if (current.id == ticketId) updatedTicket else current
    }
}

private fun MovieTicket.withLikeState(liked: Boolean): MovieTicket {
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
