package com.filmo.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.filmo.service.AppRepository
import com.filmo.service.LocalDisk
import com.filmo.service.LoginRequest
import com.filmo.service.SignupRequest
import com.filmo.ui.setup.InitialSetupStep
import com.filmo.ui.setup.InitialSetupUiState
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val localDisk: LocalDisk,
    private val appRepository: AppRepository
) : ViewModel() {
    private val _mainUiState = MutableStateFlow(MainUiState())
    val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()

    private val _initialSetupUiState = MutableStateFlow(InitialSetupUiState())
    val initialSetupUiState: StateFlow<InitialSetupUiState> = _initialSetupUiState.asStateFlow()

    val isInitialSetupFinished: StateFlow<Boolean?> = localDisk.isInitialSetupFinished
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    init {
        viewModelScope.launch {
            combine(
                localDisk.isInitialSetupFinished
            ) { initialSetupFinished ->
                initialSetupFinished
            }.collect { initialSetupFinished ->


            }

        }
    }

    fun updateInitialSetupNickname(nickname: String) {
        Timber.d(
            "MainViewModel.updateInitialSetupNickname input length=%d blank=%s",
            nickname.length,
            nickname.isBlank()
        )
        _initialSetupUiState.value = _initialSetupUiState.value.toNicknameChanged(nickname)
    }

    fun updateInitialSetupLoginId(loginId: String) {
        Timber.d(
            "MainViewModel.updateInitialSetupLoginId input length=%d blank=%s",
            loginId.length,
            loginId.isBlank()
        )
        _initialSetupUiState.value = _initialSetupUiState.value.toLoginIdChanged(loginId)
    }

    fun updateInitialSetupPassword(password: String) {
        Timber.d("MainViewModel.updateInitialSetupPassword input length=%d", password.length)
        _initialSetupUiState.value = _initialSetupUiState.value.toPasswordChanged(password)
    }

    fun startAutomaticNicknameSetup() {
        Timber.d("MainViewModel.startAutomaticNicknameSetup")
        if (_initialSetupUiState.value.isNicknameLoading) return

        _initialSetupUiState.value = _initialSetupUiState.value.toAutomaticNicknameLoading()
        viewModelScope.launch {
            appRepository.fetchRandomNickname().fold(
                onSuccess = { nickname ->
                    _initialSetupUiState.value = _initialSetupUiState.value
                        .toAutomaticNicknameLoaded(nickname)
                },
                onFailure = { throwable ->
                    Timber.w(throwable, "MainViewModel.startAutomaticNicknameSetup failed")
                    _initialSetupUiState.value = _initialSetupUiState.value.toNicknameLoadFailure()
                }
            )
        }
    }

    fun startManualNicknameSetup() {
        Timber.d("MainViewModel.startManualNicknameSetup")
        _initialSetupUiState.value = _initialSetupUiState.value.toManualNickname()
    }

    fun startLogin() {
        Timber.d("MainViewModel.startLogin")
        _initialSetupUiState.value = _initialSetupUiState.value.toLogin()
    }

    fun backToInitialSetupEntryChoice() {
        Timber.d("MainViewModel.backToInitialSetupEntryChoice")
        _initialSetupUiState.value = _initialSetupUiState.value.toBackToEntryChoice()
    }

    fun saveInitialSetupNickname() {
        val currentState = _initialSetupUiState.value
        if (!currentState.canSave) {
            Timber.d("MainViewModel.saveInitialSetupNickname ignored: canSave=false")
            return
        }

        Timber.d(
            "MainViewModel.saveInitialSetupNickname request step=%s loginIdLength=%d nicknameLength=%d",
            currentState.step,
            currentState.loginId.length,
            currentState.nickname.length
        )
        _initialSetupUiState.value = currentState.toSaving()
        viewModelScope.launch {
            val savedState = _initialSetupUiState.value
            val result = when (savedState.step) {
                InitialSetupStep.Signup -> appRepository.signUp(
                    SignupRequest(
                        loginId = savedState.loginId,
                        password = savedState.password,
                        nickname = savedState.nickname
                    )
                )

                InitialSetupStep.Login -> appRepository.login(
                    LoginRequest(
                        loginId = savedState.loginId,
                        password = savedState.password
                    )
                )

                InitialSetupStep.EntryChoice -> Result.failure(IllegalStateException("Invalid setup step"))
            }

            result.fold(
                onSuccess = { session ->
                    runCatching {
                        localDisk.setAuthSession(session)
                    }.fold(
                        onSuccess = {
                            Timber.d("MainViewModel.saveInitialSetupNickname success")
                            _initialSetupUiState.value = _initialSetupUiState.value.toSaved()
                        },
                        onFailure = { throwable ->
                            Timber.w(throwable, "MainViewModel.saveInitialSetupNickname local save failed")
                            _initialSetupUiState.value = _initialSetupUiState.value.toSaveFailure()
                        }
                    )
                },
                onFailure = { throwable ->
                    Timber.w(throwable, "MainViewModel.saveInitialSetupNickname failed")
                    _initialSetupUiState.value = _initialSetupUiState.value.toSaveFailure()
                }
            )
        }
    }

    fun pingServer() {
        if (_mainUiState.value.isPingLoading) {
            Timber.d("MainViewModel.pingServer ignored: already loading")
            return
        }

        Timber.d("MainViewModel.pingServer request")
        _mainUiState.value = _mainUiState.value.toPingLoading()
        viewModelScope.launch {
            val result = appRepository.ping()
            result
                .onSuccess { response ->
                    Timber.d("MainViewModel.pingServer success responseLength=%d", response.length)
                }
                .onFailure {
                    Timber.w(it, "MainViewModel.pingServer failed")
                }
            _mainUiState.value = result.fold(
                onSuccess = { response -> _mainUiState.value.toPingSuccess(response) },
                onFailure = { _mainUiState.value.toPingFailure() }
            )
        }
    }
}
