package minmul.androidtemplate.ui.main

data class MainUiState(
    val homeMessage: String = "Hello World",
    val isPingLoading: Boolean = false,
    val pingMessage: String? = null
)

fun MainUiState.toPingLoading(): MainUiState = copy(
    isPingLoading = true,
    pingMessage = null
)

fun MainUiState.toPingSuccess(response: String): MainUiState = copy(
    isPingLoading = false,
    pingMessage = response
        .trim()
        .takeIf { it.isNotEmpty() }
        ?.let { "서버 응답: $it" }
        ?: "서버 ping 성공"
)

fun MainUiState.toPingFailure(): MainUiState = copy(
    isPingLoading = false,
    pingMessage = "서버 ping에 실패했습니다. 잠시 후 다시 시도해 주세요."
)
