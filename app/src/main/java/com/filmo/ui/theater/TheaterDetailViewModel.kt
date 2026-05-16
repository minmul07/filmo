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
class TheaterDetailViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val theaterBookmarkStore: TheaterBookmarkStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(TheaterDetailUiState())
    val uiState: StateFlow<TheaterDetailUiState> = _uiState.asStateFlow()

    suspend fun loadTheater(theaterId: String, initiallySaved: Boolean = false) {
        if (_uiState.value.isLoading) {
            Timber.d("TheaterDetailViewModel.loadTheater ignored: already loading theaterId=%s", theaterId)
            return
        }

        val savedTheaterIds = theaterBookmarkStore.savedTheaterIds.first()
        Timber.d("TheaterDetailViewModel.loadTheater request theaterId=%s", theaterId)
        _uiState.update {
            it.copy(
                isLoading = true,
                isSaved = initiallySaved || theaterId in savedTheaterIds,
                errorMessage = null
            )
        }

        val result = appRepository.fetchTheater(theaterId)
        result
            .onSuccess { theater ->
                Timber.d("TheaterDetailViewModel.loadTheater success theaterId=%s", theater.id)
            }
            .onFailure {
                Timber.w(it, "TheaterDetailViewModel.loadTheater failed theaterId=%s", theaterId)
            }

        _uiState.update { state ->
            result.fold(
                onSuccess = { theater ->
                    state.copy(
                        theater = theater,
                        isLoading = false,
                        errorMessage = null
                    )
                },
                onFailure = {
                    state.copy(
                        isLoading = false,
                        errorMessage = "영화관 정보를 불러오지 못했어요. 다시 시도해 주세요."
                    )
                }
            )
        }
    }

    suspend fun saveTheater(theaterId: String): Boolean {
        if (_uiState.value.isBookmarkUpdating) {
            Timber.d("TheaterDetailViewModel.saveTheater ignored: updating theaterId=%s", theaterId)
            return false
        }

        Timber.d("TheaterDetailViewModel.saveTheater request theaterId=%s", theaterId)
        _uiState.update { it.copy(isBookmarkUpdating = true, bookmarkErrorMessage = null) }

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
                Timber.d("TheaterDetailViewModel.saveTheater success theaterId=%s", theaterId)
            }
            .onFailure {
                Timber.w(it, "TheaterDetailViewModel.saveTheater failed theaterId=%s", theaterId)
            }

        _uiState.update { state ->
            result.fold(
                onSuccess = {
                    state.copy(
                        isSaved = true,
                        isBookmarkUpdating = false,
                        bookmarkErrorMessage = null
                    )
                },
                onFailure = {
                    state.copy(
                        isBookmarkUpdating = false,
                        bookmarkErrorMessage = "영화관을 저장하지 못했어요. 다시 시도해 주세요."
                    )
                }
            )
        }
        return result.isSuccess
    }

    suspend fun removeSavedTheater(theaterId: String): Boolean {
        if (_uiState.value.isBookmarkUpdating) {
            Timber.d("TheaterDetailViewModel.removeSavedTheater ignored: updating theaterId=%s", theaterId)
            return false
        }

        Timber.d("TheaterDetailViewModel.removeSavedTheater request theaterId=%s", theaterId)
        _uiState.update { it.copy(isBookmarkUpdating = true, bookmarkErrorMessage = null) }

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
                Timber.d("TheaterDetailViewModel.removeSavedTheater success theaterId=%s", theaterId)
            }
            .onFailure {
                Timber.w(it, "TheaterDetailViewModel.removeSavedTheater failed theaterId=%s", theaterId)
            }

        _uiState.update { state ->
            result.fold(
                onSuccess = {
                    state.copy(
                        isSaved = false,
                        isBookmarkUpdating = false,
                        bookmarkErrorMessage = null
                    )
                },
                onFailure = {
                    state.copy(
                        isBookmarkUpdating = false,
                        bookmarkErrorMessage = "영화관 저장을 취소하지 못했어요. 다시 시도해 주세요."
                    )
                }
            )
        }
        return result.isSuccess
    }
}

data class TheaterDetailUiState(
    val theater: Theater? = null,
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val isBookmarkUpdating: Boolean = false,
    val errorMessage: String? = null,
    val bookmarkErrorMessage: String? = null
)
