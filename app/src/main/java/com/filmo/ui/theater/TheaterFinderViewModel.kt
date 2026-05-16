package com.filmo.ui.theater

import androidx.lifecycle.ViewModel
import com.filmo.service.AppRepository
import com.filmo.service.Theater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TheaterFinderViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TheaterFinderUiState())
    val uiState: StateFlow<TheaterFinderUiState> = _uiState.asStateFlow()

    suspend fun loadTheaters() {
        if (_uiState.value.isLoading) {
            Timber.d("TheaterFinderViewModel.loadTheaters ignored: already loading")
            return
        }

        Timber.d(
            "TheaterFinderViewModel.loadTheaters request page=%d size=%d",
            InitialTheaterPage,
            TheaterPageSize
        )
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        val result = appRepository.fetchTheaterPage(
            page = InitialTheaterPage,
            size = TheaterPageSize
        )
        result
            .onSuccess { page ->
                Timber.d(
                    "TheaterFinderViewModel.loadTheaters success itemCount=%d hasMore=%s",
                    page.items.size,
                    page.hasMore
                )
            }
            .onFailure {
                Timber.w(it, "TheaterFinderViewModel.loadTheaters failed")
            }

        _uiState.update { state ->
            result.fold(
                onSuccess = { page ->
                    state.copy(
                        theaters = page.items,
                        theaterPage = InitialTheaterPage,
                        canLoadMore = page.hasMore,
                        isLoading = false,
                        isAppending = false,
                        errorMessage = null
                    )
                },
                onFailure = {
                    state.copy(
                        isLoading = false,
                        errorMessage = "영화관 목록을 불러오지 못했어요. 다시 시도해 주세요."
                    )
                }
            )
        }
    }

    suspend fun loadNextTheaterPage() {
        val currentState = _uiState.value
        if (currentState.isLoading ||
            currentState.isAppending ||
            !currentState.canLoadMore
        ) {
            Timber.d(
                "TheaterFinderViewModel.loadNextTheaterPage ignored loading=%s appending=%s canLoadMore=%s",
                currentState.isLoading,
                currentState.isAppending,
                currentState.canLoadMore
            )
            return
        }

        val nextPage = currentState.theaterPage + 1
        Timber.d(
            "TheaterFinderViewModel.loadNextTheaterPage request page=%d size=%d",
            nextPage,
            TheaterPageSize
        )
        _uiState.update {
            it.copy(
                isAppending = true,
                errorMessage = null
            )
        }

        val result = appRepository.fetchTheaterPage(
            page = nextPage,
            size = TheaterPageSize
        )
        result
            .onSuccess { page ->
                Timber.d(
                    "TheaterFinderViewModel.loadNextTheaterPage success page=%d itemCount=%d hasMore=%s",
                    nextPage,
                    page.items.size,
                    page.hasMore
                )
            }
            .onFailure {
                Timber.w(it, "TheaterFinderViewModel.loadNextTheaterPage failed page=%d", nextPage)
            }

        _uiState.update { state ->
            result.fold(
                onSuccess = { page ->
                    state.copy(
                        theaters = state.theaters + page.items,
                        theaterPage = nextPage,
                        canLoadMore = page.hasMore,
                        isAppending = false,
                        errorMessage = null
                    )
                },
                onFailure = {
                    state.copy(
                        isAppending = false,
                        errorMessage = "영화관 목록을 더 불러오지 못했어요. 다시 시도해 주세요."
                    )
                }
            )
        }
    }

    fun toggleSaved(theaterId: String) {
        Timber.d("TheaterFinderViewModel.toggleSaved theaterId=%s", theaterId)
        _uiState.update { state ->
            val savedIds = if (theaterId in state.savedTheaterIds) {
                state.savedTheaterIds - theaterId
            } else {
                state.savedTheaterIds + theaterId
            }
            state.copy(savedTheaterIds = savedIds)
        }
    }
}

data class TheaterFinderUiState(
    val theaters: List<Theater> = emptyList(),
    val savedTheaterIds: Set<String> = emptySet(),
    val theaterPage: Int = InitialTheaterPage,
    val canLoadMore: Boolean = true,
    val isLoading: Boolean = false,
    val isAppending: Boolean = false,
    val errorMessage: String? = null
)

private const val InitialTheaterPage = 0
private const val TheaterPageSize = 20
