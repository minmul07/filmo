package com.filmo.ui.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmo.service.AppRepository
import com.filmo.service.CreateTicketRequest
import com.filmo.service.MovieCatalogItem
import com.filmo.service.MovieDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class RegisterMovieViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterMovieUiState())
    val uiState: StateFlow<RegisterMovieUiState> = _uiState.asStateFlow()

    suspend fun loadMovieCatalog() {
        if (_uiState.value.isMovieCatalogLoading) {
            Timber.d("RegisterMovieViewModel.loadMovieCatalog ignored: already loading")
            return
        }

        Timber.d(
            "RegisterMovieViewModel.loadMovieCatalog request page=%d size=%d",
            InitialMovieCatalogPage,
            MovieCatalogPageSize
        )
        _uiState.update {
            it.copy(
                isMovieCatalogLoading = true,
                errorMessage = null
            )
        }

        val result = appRepository.fetchMovieCatalogPage(
            page = InitialMovieCatalogPage,
            size = MovieCatalogPageSize
        )
        result
            .onSuccess { page ->
                Timber.d(
                    "RegisterMovieViewModel.loadMovieCatalog success page=%d itemCount=%d hasMore=%s",
                    InitialMovieCatalogPage,
                    page.items.size,
                    page.hasMore
                )
            }
            .onFailure {
                Timber.w(it, "RegisterMovieViewModel.loadMovieCatalog failed page=%d", InitialMovieCatalogPage)
            }
        _uiState.update { state ->
            result.fold(
                onSuccess = { page ->
                    state.copy(
                        movies = page.items,
                        movieCatalogPage = InitialMovieCatalogPage,
                        canLoadMoreMovies = page.hasMore,
                        isMovieCatalogLoading = false,
                        isMovieCatalogAppendLoading = false,
                        errorMessage = null
                    )
                },
                onFailure = {
                    state.copy(
                        isMovieCatalogLoading = false,
                        errorMessage = "영화 목록을 불러오지 못했어요. 다시 시도해 주세요."
                    )
                }
            )
        }
    }

    suspend fun loadNextMovieCatalogPage() {
        val currentState = _uiState.value
        if (currentState.isMovieCatalogLoading ||
            currentState.isMovieCatalogAppendLoading ||
            !currentState.canLoadMoreMovies
        ) {
            Timber.d(
                "RegisterMovieViewModel.loadNextMovieCatalogPage ignored loading=%s appendLoading=%s canLoadMore=%s",
                currentState.isMovieCatalogLoading,
                currentState.isMovieCatalogAppendLoading,
                currentState.canLoadMoreMovies
            )
            return
        }

        val nextPage = currentState.movieCatalogPage + 1
        Timber.d(
            "RegisterMovieViewModel.loadNextMovieCatalogPage request page=%d size=%d",
            nextPage,
            MovieCatalogPageSize
        )
        _uiState.update {
            it.copy(
                isMovieCatalogAppendLoading = true,
                errorMessage = null
            )
        }

        val result = appRepository.fetchMovieCatalogPage(
            page = nextPage,
            size = MovieCatalogPageSize
        )
        result
            .onSuccess { page ->
                Timber.d(
                    "RegisterMovieViewModel.loadNextMovieCatalogPage success page=%d itemCount=%d hasMore=%s",
                    nextPage,
                    page.items.size,
                    page.hasMore
                )
            }
            .onFailure {
                Timber.w(it, "RegisterMovieViewModel.loadNextMovieCatalogPage failed page=%d", nextPage)
            }
        _uiState.update { state ->
            result.fold(
                onSuccess = { page ->
                    state.copy(
                        movies = state.movies + page.items,
                        movieCatalogPage = nextPage,
                        canLoadMoreMovies = page.hasMore,
                        isMovieCatalogAppendLoading = false,
                        errorMessage = null
                    )
                },
                onFailure = {
                    state.copy(
                        isMovieCatalogAppendLoading = false,
                        errorMessage = "영화 목록을 더 불러오지 못했어요. 다시 시도해 주세요."
                    )
                }
            )
        }
    }

    fun updateSearchQuery(query: String) {
        Timber.d(
            "RegisterMovieViewModel.updateSearchQuery input length=%d blank=%s",
            query.length,
            query.isBlank()
        )
        _uiState.update { it.copy(searchQuery = query, errorMessage = null) }
    }

    fun selectMovie(movie: MovieCatalogItem) {
        Timber.d("RegisterMovieViewModel.selectMovie movieId=%s", movie.id)
        _uiState.update {
            it.copy(
                step = RegisterMovieStep.MovieInfo,
                selectedMovie = movie,
                selectedMovieDetail = null,
                isMovieDetailLoading = true,
                title = movie.title,
                genre = movie.genre,
                director = movie.director,
                releaseDateMillis = it.releaseDateMillis ?: todayStartMillis(),
                errorMessage = null
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            Timber.d("RegisterMovieViewModel.fetchMovieDetail request movieId=%s", movie.id)
            val result = appRepository.fetchMovieDetail(movie.id)
            result
                .onSuccess { detail ->
                    Timber.d("RegisterMovieViewModel.fetchMovieDetail success movieId=%s detailId=%s", movie.id, detail.id)
                }
                .onFailure {
                    Timber.w(it, "RegisterMovieViewModel.fetchMovieDetail failed movieId=%s", movie.id)
                }
            _uiState.update { state ->
                if (state.selectedMovie?.id != movie.id) {
                    Timber.d("RegisterMovieViewModel.fetchMovieDetail ignored stale result movieId=%s", movie.id)
                    return@update state
                }

                result.fold(
                    onSuccess = { detail ->
                        state.copy(
                            selectedMovieDetail = detail,
                            isMovieDetailLoading = false,
                            title = detail.title.ifBlank { state.title },
                            genre = detail.genre.ifBlank { state.genre },
                            director = detail.director.ifBlank { state.director }
                        )
                    },
                    onFailure = {
                        state.copy(isMovieDetailLoading = false)
                    }
                )
            }
        }
    }

    fun applySearchResult(
        title: String,
        genre: String,
        director: String,
        cast: String
    ) {
        // TODO: Wire this to the backend movie search result when the API is ready.
        Timber.d(
            "RegisterMovieViewModel.applySearchResult input titleLength=%d genreLength=%d directorLength=%d castLength=%d",
            title.length,
            genre.length,
            director.length,
            cast.length
        )
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
        Timber.d("RegisterMovieViewModel.updateTitle input length=%d blank=%s", title.length, title.isBlank())
        _uiState.update { it.copy(title = title, errorMessage = null) }
    }

    fun updateReleaseDateMillis(
        releaseDateMillis: Long?,
        nowMillis: Long = System.currentTimeMillis()
    ) {
        if (releaseDateMillis != null && isFutureDate(releaseDateMillis, nowMillis)) {
            Timber.d("RegisterMovieViewModel.updateReleaseDateMillis rejected futureDate=true")
            _uiState.update {
                it.copy(
                    releaseDateMillis = null,
                    errorMessage = "미래 날짜는 선택할 수 없어요."
                )
            }
            return
        }

        Timber.d("RegisterMovieViewModel.updateReleaseDateMillis input hasDate=%s", releaseDateMillis != null)
        _uiState.update {
            it.copy(
                releaseDateMillis = releaseDateMillis,
                errorMessage = null
            )
        }
    }

    fun updateGenre(genre: String) {
        Timber.d("RegisterMovieViewModel.updateGenre input length=%d blank=%s", genre.length, genre.isBlank())
        _uiState.update { it.copy(genre = genre, errorMessage = null) }
    }

    fun updateDirector(director: String) {
        Timber.d("RegisterMovieViewModel.updateDirector input length=%d blank=%s", director.length, director.isBlank())
        _uiState.update { it.copy(director = director, errorMessage = null) }
    }

    fun updateCast(cast: String) {
        Timber.d("RegisterMovieViewModel.updateCast input length=%d blank=%s", cast.length, cast.isBlank())
        _uiState.update { it.copy(cast = cast, errorMessage = null) }
    }

    fun updateRating(rating: Int) {
        Timber.d("RegisterMovieViewModel.updateRating input=%d coerced=%d", rating, rating.coerceIn(MIN_RATING, MAX_RATING))
        _uiState.update {
            it.copy(
                rating = rating.coerceIn(MIN_RATING, MAX_RATING),
                errorMessage = null
            )
        }
    }

    fun updateReview(review: String) {
        Timber.d(
            "RegisterMovieViewModel.updateReview input length=%d storedLength=%d",
            review.length,
            review.take(MAX_REVIEW_LENGTH).length
        )
        _uiState.update {
            it.copy(
                review = review.take(MAX_REVIEW_LENGTH),
                errorMessage = null
            )
        }
    }

    fun goToNextStep() {
        Timber.d("RegisterMovieViewModel.goToNextStep currentStep=%s", _uiState.value.step)
        when (_uiState.value.step) {
            RegisterMovieStep.MovieSearch -> {
                Timber.d("RegisterMovieViewModel.goToNextStep blocked reason=no_movie_selected")
                _uiState.update { it.copy(errorMessage = "영화를 선택해 주세요.") }
            }

            RegisterMovieStep.MovieInfo -> moveToShareIfValid()
            RegisterMovieStep.Share -> Unit
        }
    }

    suspend fun createTicket(): Boolean {
        val state = _uiState.value
        if (state.isTicketCreateLoading) {
            Timber.d("RegisterMovieViewModel.createTicket ignored: already loading")
            return false
        }

        val request = state.toCreateTicketRequestOrNull()
        if (request == null) {
            Timber.d("RegisterMovieViewModel.createTicket blocked reason=missing_required_info")
            _uiState.update {
                it.copy(errorMessage = RequiredViewingInfoMessage)
            }
            return false
        }

        Timber.d(
            "RegisterMovieViewModel.createTicket request movieId=%s watchedDateLength=%d cinemaLength=%d reviewLength=%d",
            request.movieId,
            request.watchedDate.length,
            request.cinema.length,
            request.review.length
        )
        _uiState.update {
            it.copy(
                isTicketCreateLoading = true,
                errorMessage = null
            )
        }

        val result = appRepository.createTicket(request)
        result
            .onSuccess {
                Timber.d("RegisterMovieViewModel.createTicket success movieId=%s", request.movieId)
            }
            .onFailure {
                Timber.w(it, "RegisterMovieViewModel.createTicket failed movieId=%s", request.movieId)
            }
        _uiState.update { current ->
            result.fold(
                onSuccess = {
                    current.copy(
                        isTicketCreateLoading = false,
                        errorMessage = null
                    )
                },
                onFailure = {
                    current.copy(
                        isTicketCreateLoading = false,
                        errorMessage = "티켓을 생성하지 못했어요. 다시 시도해 주세요."
                    )
                }
            )
        }

        return result.isSuccess
    }

    fun goBack(): Boolean {
        val currentState = _uiState.value
        val previousStep = when (currentState.step) {
            RegisterMovieStep.MovieSearch -> return false
            RegisterMovieStep.MovieInfo -> RegisterMovieStep.MovieSearch
            RegisterMovieStep.Share -> RegisterMovieStep.MovieInfo
        }

        _uiState.update {
            it.copy(
                step = previousStep,
                errorMessage = null
            )
        }
        Timber.d("RegisterMovieViewModel.goBack moved from=%s to=%s", currentState.step, previousStep)
        return true
    }

    fun reset() {
        Timber.d("RegisterMovieViewModel.reset")
        _uiState.value = RegisterMovieUiState()
    }

    private fun moveToShareIfValid() {
        val state = _uiState.value
        val missingRequiredInfo = state.selectedMovie == null ||
            state.releaseDateMillis == null ||
            state.rating == null ||
            state.review.isBlank()

        if (missingRequiredInfo) {
            Timber.d(
                "RegisterMovieViewModel.moveToShareIfValid blocked selectedMovie=%s hasDate=%s hasRating=%s reviewBlank=%s",
                state.selectedMovie != null,
                state.releaseDateMillis != null,
                state.rating != null,
                state.review.isBlank()
            )
            _uiState.update {
                it.copy(errorMessage = RequiredViewingInfoMessage)
            }
            return
        }

        Timber.d("RegisterMovieViewModel.moveToShareIfValid success")
        _uiState.update {
            it.copy(
                step = RegisterMovieStep.Share,
                errorMessage = null
            )
        }
    }
}

data class RegisterMovieUiState(
    val step: RegisterMovieStep = RegisterMovieStep.MovieSearch,
    val searchQuery: String = "",
    val movies: List<MovieCatalogItem> = emptyList(),
    val movieCatalogPage: Int = 0,
    val canLoadMoreMovies: Boolean = true,
    val isMovieCatalogLoading: Boolean = false,
    val isMovieCatalogAppendLoading: Boolean = false,
    val selectedMovie: MovieCatalogItem? = null,
    val selectedMovieDetail: MovieDetail? = null,
    val isMovieDetailLoading: Boolean = false,
    val isTicketCreateLoading: Boolean = false,
    val title: String = "",
    val releaseDateMillis: Long? = null,
    val genre: String = "",
    val director: String = "",
    val cast: String = "",
    val rating: Int? = null,
    val review: String = "",
    val errorMessage: String? = null
) {
    val filteredMovies: List<MovieCatalogItem>
        get() {
            val trimmedQuery = searchQuery.trim()
            if (trimmedQuery.isEmpty()) return movies

            return movies.filter { movie ->
                movie.title.contains(trimmedQuery, ignoreCase = true) ||
                    movie.director.contains(trimmedQuery, ignoreCase = true)
            }
        }
}

private fun RegisterMovieUiState.toCreateTicketRequestOrNull(): CreateTicketRequest? {
    val movieId = selectedMovie?.id?.takeIf { it.isNotBlank() } ?: return null
    val watchedDate = releaseDateMillis.toApiWatchedDate().takeIf { it.isNotBlank() } ?: return null
    val trimmedReview = review.trim().takeIf { it.isNotBlank() } ?: return null

    return CreateTicketRequest(
        movieId = movieId,
        watchedDate = watchedDate,
        watchedTime = DefaultWatchedTime,
        cinema = DefaultCinema,
        review = trimmedReview
    )
}

enum class RegisterMovieStep {
    MovieSearch,
    MovieInfo,
    Share
}

private const val InitialMovieCatalogPage = 1
private const val MovieCatalogPageSize = 20
private const val MIN_RATING = 1
private const val MAX_RATING = 5
private const val MAX_REVIEW_LENGTH = 100
private const val DefaultWatchedTime = "00:00"
private const val DefaultCinema = ""
private const val RequiredViewingInfoMessage = "관람일, 별점, 관람 후기를 입력해 주세요."

private fun isFutureDate(dateMillis: Long, nowMillis: Long): Boolean {
    return dateMillis > nowMillis
}

private fun todayStartMillis(): Long {
    return java.time.LocalDate.now()
        .atStartOfDay(java.time.ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

private fun Long?.toApiWatchedDate(): String {
    return this?.let { millis ->
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
    }.orEmpty()
}
