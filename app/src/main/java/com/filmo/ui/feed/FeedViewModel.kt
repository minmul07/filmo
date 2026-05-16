package com.filmo.ui.feed

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
class FeedViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    suspend fun loadPublicTickets() {
        if (_uiState.value.isLoading) {
            Timber.d("FeedViewModel.loadPublicTickets ignored: already loading")
            return
        }

        Timber.d("FeedViewModel.loadPublicTickets request")
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        val result = appRepository.fetchPublicTickets()
        result
            .onSuccess { tickets ->
                Timber.d("FeedViewModel.loadPublicTickets success count=%d", tickets.size)
            }
            .onFailure {
                Timber.w(it, "FeedViewModel.loadPublicTickets failed")
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
}

data class FeedUiState(
    val tickets: List<PublicTicket> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
