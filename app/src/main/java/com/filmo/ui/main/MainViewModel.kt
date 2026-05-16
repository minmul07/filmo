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
import com.filmo.ui.setup.InitialSetupUiState
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
        _initialSetupUiState.value = _initialSetupUiState.value.toNicknameChanged(nickname)
    }

    fun saveInitialSetupNickname() {
        val currentState = _initialSetupUiState.value
        if (!currentState.canSave) return

        _initialSetupUiState.value = currentState.toSaving()
        viewModelScope.launch {
            val nickname = _initialSetupUiState.value.nickname
            runCatching {
                localDisk.setNickname(nickname)
            }.fold(
                onSuccess = {
                    _initialSetupUiState.value = _initialSetupUiState.value.toSaved()
                },
                onFailure = {
                    _initialSetupUiState.value = _initialSetupUiState.value.toSaveFailure()
                }
            )
        }
    }

    fun pingServer() {
        if (_mainUiState.value.isPingLoading) return

        _mainUiState.value = _mainUiState.value.toPingLoading()
        viewModelScope.launch {
            val result = appRepository.ping()
            _mainUiState.value = result.fold(
                onSuccess = { response -> _mainUiState.value.toPingSuccess(response) },
                onFailure = { _mainUiState.value.toPingFailure() }
            )
        }
    }
}
