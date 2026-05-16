package com.filmo.ui.theater

import androidx.lifecycle.ViewModel
import com.filmo.service.AppRepository
import com.filmo.service.Theater
import com.filmo.service.TheaterBookmarkStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TheaterFinderViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val theaterBookmarkStore: TheaterBookmarkStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(TheaterFinderUiState())
    val uiState: StateFlow<TheaterFinderUiState> = _uiState.asStateFlow()

    suspend fun loadTheaters() {
        if (_uiState.value.isLoading) {
            Timber.d("TheaterFinderViewModel.loadTheaters ignored: already loading")
            return
        }

        val savedTheaterIds = theaterBookmarkStore.savedTheaterIds.first()
        Timber.d(
            "TheaterFinderViewModel.loadTheaters request page=%d size=%d",
            InitialTheaterPage,
            TheaterPageSize
        )
        _uiState.update {
            it.copy(
                isLoading = true,
                savedTheaterIds = savedTheaterIds,
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

    suspend fun saveTheater(theaterId: String): Boolean {
        val currentState = _uiState.value
        if (theaterId in currentState.updatingBookmarkTheaterIds) {
            Timber.d("TheaterFinderViewModel.saveTheater ignored: updating theaterId=%s", theaterId)
            return false
        }

        Timber.d("TheaterFinderViewModel.saveTheater request theaterId=%s", theaterId)
        _uiState.update { state ->
            state.copy(
                updatingBookmarkTheaterIds = state.updatingBookmarkTheaterIds + theaterId,
                bookmarkErrorMessage = null
            )
        }

        val result = appRepository.saveTheater(theaterId).fold(
            onSuccess = {
                runCatching {
                    theaterBookmarkStore.saveTheaterId(theaterId)
                }
            },
            onFailure = { Result.failure(it) }
        )
        result
            .onSuccess {
                Timber.d("TheaterFinderViewModel.saveTheater success theaterId=%s", theaterId)
            }
            .onFailure {
                Timber.w(it, "TheaterFinderViewModel.saveTheater failed theaterId=%s", theaterId)
            }

        _uiState.update { state ->
            result.fold(
                onSuccess = {
                    state.copy(
                        savedTheaterIds = state.savedTheaterIds + theaterId,
                        updatingBookmarkTheaterIds = state.updatingBookmarkTheaterIds - theaterId,
                        bookmarkErrorMessage = null
                    )
                },
                onFailure = {
                    state.copy(
                        updatingBookmarkTheaterIds = state.updatingBookmarkTheaterIds - theaterId,
                        bookmarkErrorMessage = "영화관을 저장하지 못했어요. 다시 시도해 주세요."
                    )
                }
            )
        }
        return result.isSuccess
    }

    suspend fun removeSavedTheater(theaterId: String): Boolean {
        val currentState = _uiState.value
        if (theaterId in currentState.updatingBookmarkTheaterIds) {
            Timber.d("TheaterFinderViewModel.removeSavedTheater ignored: updating theaterId=%s", theaterId)
            return false
        }

        Timber.d("TheaterFinderViewModel.removeSavedTheater request theaterId=%s", theaterId)
        _uiState.update { state ->
            state.copy(
                updatingBookmarkTheaterIds = state.updatingBookmarkTheaterIds + theaterId,
                bookmarkErrorMessage = null
            )
        }

        val result = appRepository.removeSavedTheater(theaterId).fold(
            onSuccess = {
                runCatching {
                    theaterBookmarkStore.removeTheaterId(theaterId)
                }
            },
            onFailure = { Result.failure(it) }
        )
        result
            .onSuccess {
                Timber.d("TheaterFinderViewModel.removeSavedTheater success theaterId=%s", theaterId)
            }
            .onFailure {
                Timber.w(it, "TheaterFinderViewModel.removeSavedTheater failed theaterId=%s", theaterId)
            }

        _uiState.update { state ->
            result.fold(
                onSuccess = {
                    state.copy(
                        savedTheaterIds = state.savedTheaterIds - theaterId,
                        updatingBookmarkTheaterIds = state.updatingBookmarkTheaterIds - theaterId,
                        bookmarkErrorMessage = null
                    )
                },
                onFailure = {
                    state.copy(
                        updatingBookmarkTheaterIds = state.updatingBookmarkTheaterIds - theaterId,
                        bookmarkErrorMessage = "영화관 저장을 취소하지 못했어요. 다시 시도해 주세요."
                    )
                }
            )
        }
        return result.isSuccess
    }
}

data class TheaterFinderUiState(
    val theaters: List<Theater> = emptyList(),
    val savedTheaterIds: Set<String> = emptySet(),
    val updatingBookmarkTheaterIds: Set<String> = emptySet(),
    val theaterPage: Int = InitialTheaterPage,
    val canLoadMore: Boolean = true,
    val isLoading: Boolean = false,
    val isAppending: Boolean = false,
    val errorMessage: String? = null,
    val bookmarkErrorMessage: String? = null
)

private const val InitialTheaterPage = 0
private const val TheaterPageSize = 20
