package com.filmo.ui.movie

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class RegisterMovieViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterMovieUiState())
    val uiState: StateFlow<RegisterMovieUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateReleaseDateMillis(releaseDateMillis: Long?) {
        _uiState.update { it.copy(releaseDateMillis = releaseDateMillis) }
    }

    fun updateGenre(genre: String) {
        _uiState.update { it.copy(genre = genre) }
    }

    fun updateDirector(director: String) {
        _uiState.update { it.copy(director = director) }
    }

    fun updateCast(cast: String) {
        _uiState.update { it.copy(cast = cast) }
    }
}

data class RegisterMovieUiState(
    val title: String = "",
    val releaseDateMillis: Long? = null,
    val genre: String = "",
    val director: String = "",
    val cast: String = ""
)
