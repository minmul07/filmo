package minmul.androidtemplate.ui.main

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
import minmul.androidtemplate.service.AppRepository
import minmul.androidtemplate.service.LocalDisk
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val localDisk: LocalDisk,
    private val appRepository: AppRepository
) : ViewModel() {
    private val _mainUiState = MutableStateFlow(MainUiState())
    val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()

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
