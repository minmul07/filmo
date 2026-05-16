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

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query, errorMessage = null) }
    }

    fun applySearchResult(
        title: String,
        genre: String,
        director: String,
        cast: String
    ) {
        // TODO: Wire this to the backend movie search result when the API is ready.
        _uiState.update {
            it.copy(
                title = title,
                genre = genre,
                director = director,
                cast = cast,
                errorMessage = null
            )
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, errorMessage = null) }
    }

    fun updateReleaseDateMillis(
        releaseDateMillis: Long?,
        nowMillis: Long = System.currentTimeMillis()
    ) {
        if (releaseDateMillis != null && isFutureDate(releaseDateMillis, nowMillis)) {
            _uiState.update {
                it.copy(
                    releaseDateMillis = null,
                    errorMessage = "미래 날짜는 선택할 수 없어요."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                releaseDateMillis = releaseDateMillis,
                errorMessage = null
            )
        }
    }

    fun updateGenre(genre: String) {
        _uiState.update { it.copy(genre = genre, errorMessage = null) }
    }

    fun updateDirector(director: String) {
        _uiState.update { it.copy(director = director, errorMessage = null) }
    }

    fun updateCast(cast: String) {
        _uiState.update { it.copy(cast = cast, errorMessage = null) }
    }

    fun selectTicketTemplate(template: TicketTemplateOption) {
        _uiState.update {
            it.copy(
                selectedTicketTemplate = template,
                errorMessage = null
            )
        }
    }

    fun goToNextStep(nowMillis: Long = System.currentTimeMillis()) {
        when (_uiState.value.step) {
            RegisterMovieStep.MovieSearch -> {
                _uiState.update {
                    it.copy(
                        step = RegisterMovieStep.MovieInfo,
                        errorMessage = null
                    )
                }
            }

            RegisterMovieStep.MovieInfo -> moveToTicketTemplateIfValid()
            RegisterMovieStep.TicketTemplate -> beginPublishingIfValid(nowMillis)
            RegisterMovieStep.Publishing -> Unit
        }
    }

    fun goBack(): Boolean {
        val currentState = _uiState.value
        if (currentState.step == RegisterMovieStep.Publishing &&
            currentState.publishingStatus == PublishingStatus.Publishing
        ) {
            return true
        }

        val previousStep = when (currentState.step) {
            RegisterMovieStep.MovieSearch -> return false
            RegisterMovieStep.MovieInfo -> RegisterMovieStep.MovieSearch
            RegisterMovieStep.TicketTemplate -> RegisterMovieStep.MovieInfo
            RegisterMovieStep.Publishing -> RegisterMovieStep.TicketTemplate
        }

        _uiState.update {
            it.copy(
                step = previousStep,
                errorMessage = null,
                publishStartedAtMillis = null,
                publishCompletedAtMillis = null,
                publishingStatus = PublishingStatus.Idle
            )
        }
        return true
    }

    fun markPublishingComplete(nowMillis: Long = System.currentTimeMillis()) {
        _uiState.update {
            if (it.step == RegisterMovieStep.Publishing &&
                it.publishingStatus == PublishingStatus.Publishing
            ) {
                it.copy(
                    publishingStatus = PublishingStatus.Complete,
                    publishCompletedAtMillis = nowMillis
                )
            } else {
                it
            }
        }
    }

    fun reset() {
        _uiState.value = RegisterMovieUiState()
    }

    private fun moveToTicketTemplateIfValid() {
        val state = _uiState.value
        val missingRequiredInfo = state.title.isBlank() ||
            state.releaseDateMillis == null ||
            state.genre.isBlank()

        if (missingRequiredInfo) {
            _uiState.update {
                it.copy(errorMessage = "영화 제목, 관람 날짜, 장르를 입력해 주세요.")
            }
            return
        }

        _uiState.update {
            it.copy(
                step = RegisterMovieStep.TicketTemplate,
                errorMessage = null
            )
        }
    }

    private fun beginPublishingIfValid(nowMillis: Long) {
        val state = _uiState.value
        if (state.selectedTicketTemplate == null) {
            _uiState.update { it.copy(errorMessage = "티켓 디자인을 선택해 주세요.") }
            return
        }

        _uiState.update {
            it.copy(
                step = RegisterMovieStep.Publishing,
                publishingStatus = PublishingStatus.Publishing,
                publishStartedAtMillis = nowMillis,
                publishCompletedAtMillis = null,
                errorMessage = null
            )
        }
    }
}

data class RegisterMovieUiState(
    val step: RegisterMovieStep = RegisterMovieStep.MovieSearch,
    val searchQuery: String = "",
    val title: String = "",
    val releaseDateMillis: Long? = null,
    val genre: String = "",
    val director: String = "",
    val cast: String = "",
    val selectedTicketTemplate: TicketTemplateOption? = null,
    val publishingStatus: PublishingStatus = PublishingStatus.Idle,
    val publishStartedAtMillis: Long? = null,
    val publishCompletedAtMillis: Long? = null,
    val errorMessage: String? = null
) {
    val isPublishComplete: Boolean
        get() = publishingStatus == PublishingStatus.Complete
}

enum class RegisterMovieStep {
    MovieSearch,
    MovieInfo,
    TicketTemplate,
    Publishing
}

enum class TicketTemplateOption(
    val label: String,
    val description: String
) {
    Classic(
        label = "Classic",
        description = "독립영화 티켓 기본형"
    ),
    Poster(
        label = "Poster",
        description = "포스터 중심 티켓"
    ),
    Minimal(
        label = "Minimal",
        description = "간결한 수집 카드"
    ),
    Archive(
        label = "Archive",
        description = "기록 보관형 티켓"
    )
}

enum class PublishingStatus {
    Idle,
    Publishing,
    Complete
}

internal const val TicketPublishingDurationMillis = 2_000L

internal fun publishingProgressPercent(startedAtMillis: Long?, nowMillis: Long): Int {
    if (startedAtMillis == null) return 0

    val elapsedMillis = (nowMillis - startedAtMillis).coerceAtLeast(0L)
    return ((elapsedMillis * 100) / TicketPublishingDurationMillis)
        .coerceIn(0L, 100L)
        .toInt()
}

private fun isFutureDate(dateMillis: Long, nowMillis: Long): Boolean {
    return dateMillis > nowMillis
}
