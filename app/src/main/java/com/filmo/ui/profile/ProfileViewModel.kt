package com.filmo.ui.profile

import androidx.lifecycle.ViewModel
import com.filmo.service.AppRepository
import com.filmo.service.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    suspend fun loadProfile() {
        if (_uiState.value.isLoading) {
            Timber.d("ProfileViewModel.loadProfile ignored: already loading")
            return
        }

        Timber.d("ProfileViewModel.loadProfile request")
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        val result = appRepository.fetchMyProfile()
        result
            .onSuccess { profile ->
                Timber.d("ProfileViewModel.loadProfile success profileId=%s", profile.id)
            }
            .onFailure {
                Timber.w(it, "ProfileViewModel.loadProfile failed")
            }

        _uiState.update { state ->
            result.fold(
                onSuccess = { profile ->
                    state.copy(
                        profile = profile,
                        isLoading = false,
                        errorMessage = null
                    )
                },
                onFailure = {
                    state.copy(
                        isLoading = false,
                        errorMessage = "프로필을 불러오지 못했어요. 다시 시도해 주세요."
                    )
                }
            )
        }
    }
}

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
