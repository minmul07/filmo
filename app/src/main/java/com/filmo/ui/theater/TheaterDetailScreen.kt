package com.filmo.ui.theater

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.LocalMovies
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filmo.service.Theater
import com.filmo.ui.theme.FilmoTheme
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun TheaterDetailScreen(
    theaterId: String,
    initiallySaved: Boolean,
    onBack: () -> Unit,
    syncedIsSaved: Boolean? = null,
    onBookmarkChanged: (theaterId: String, saved: Boolean) -> Unit = { _, _ -> },
    viewModel: TheaterDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(theaterId) {
        viewModel.loadTheater(theaterId, initiallySaved)
    }

    TheaterDetailContent(
        uiState = if (syncedIsSaved == null) {
            uiState
        } else {
            uiState.copy(isSaved = syncedIsSaved)
        },
        theaterId = theaterId,
        onBack = {
            Timber.d("TheaterDetailScreen.backClick theaterId=%s", theaterId)
            onBack()
        },
        onRetryClick = {
            Timber.d("TheaterDetailScreen.retryClick theaterId=%s", theaterId)
            coroutineScope.launch {
                viewModel.loadTheater(theaterId, initiallySaved)
            }
        },
        onBookmarkClick = {
            Timber.d(
                "TheaterDetailScreen.bookmarkClick theaterId=%s saved=%s",
                theaterId,
                uiState.isSaved
            )
            coroutineScope.launch {
                val currentSaved = syncedIsSaved ?: uiState.isSaved
                val changed = if (currentSaved) {
                    viewModel.removeSavedTheater(theaterId)
                } else {
                    viewModel.saveTheater(theaterId)
                }
                if (changed) {
                    onBookmarkChanged(theaterId, !currentSaved)
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun TheaterDetailContent(
    uiState: TheaterDetailUiState,
    theaterId: String,
    onBack: () -> Unit,
    onRetryClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                top = 18.dp,
                end = 24.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(
                key = "theater_detail_top_bar",
                contentType = "top_bar"
            ) {
                TheaterDetailTopBar(
                    title = uiState.theater?.name?.ifBlank { theaterId } ?: theaterId,
                    isSaved = uiState.isSaved,
                    isBookmarkUpdating = uiState.isBookmarkUpdating,
                    onBack = onBack,
                    onBookmarkClick = onBookmarkClick
                )
            }

            if (uiState.isLoading) {
                item(
                    key = "theater_detail_loading",
                    contentType = "loading"
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            if (uiState.bookmarkErrorMessage != null) {
                item(
                    key = "theater_detail_bookmark_error",
                    contentType = "inline_error"
                ) {
                    Text(
                        text = uiState.bookmarkErrorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            when {
                uiState.errorMessage != null -> item(
                    key = "theater_detail_error",
                    contentType = "placeholder"
                ) {
                    TheaterDetailPlaceholder(
                        message = uiState.errorMessage,
                        onRetryClick = onRetryClick
                    )
                }

                uiState.theater == null && !uiState.isLoading -> item(
                    key = "theater_detail_empty",
                    contentType = "placeholder"
                ) {
                    TheaterDetailPlaceholder(
                        message = "표시할 영화관 정보가 없어요.",
                        onRetryClick = onRetryClick
                    )
                }

                uiState.theater != null -> {
                    item(
                        key = "theater_detail_summary",
                        contentType = "summary"
                    ) {
                        TheaterDetailSummary(theater = uiState.theater)
                    }

                    item(
                        key = "theater_detail_api_title",
                        contentType = "section_title"
                    ) {
                        Text(
                            text = "API 항목",
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    items(
                        items = uiState.theater.apiFields,
                        key = { it.name },
                        contentType = { "api_field" }
                    ) { field ->
                        TheaterApiFieldRow(field = field)
                    }
                }
            }
        }
    }
}

@Composable
private fun TheaterDetailTopBar(
    title: String,
    isSaved: Boolean,
    isBookmarkUpdating: Boolean,
    onBack: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로 가기"
            )
        }

        Text(
            text = title.ifBlank { "영화관 상세" },
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        IconButton(
            onClick = onBookmarkClick,
            modifier = Modifier.size(44.dp),
            enabled = !isBookmarkUpdating
        ) {
            Icon(
                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (isSaved) "저장 취소" else "영화관 저장",
                tint = if (isSaved) {
                    MaterialTheme.colorScheme.primary
                } else if (isBookmarkUpdating) {
                    MaterialTheme.colorScheme.outlineVariant
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

@Composable
private fun TheaterDetailSummary(theater: Theater) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = theater.name.ifBlank { "이름 없는 영화관" },
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = theater.address.ifBlank { "주소 정보 없음" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = listOf(theater.screenName, theater.screenType)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
                    .ifBlank { "상영관 정보 없음" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TheaterApiFieldRow(field: TheaterApiField) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = field.name,
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = field.value.ifBlank { "-" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = field.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TheaterDetailPlaceholder(
    message: String,
    onRetryClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalMovies,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(onClick = onRetryClick) {
                Text("다시 시도")
            }
        }
    }
}

private data class TheaterApiField(
    val name: String,
    val label: String,
    val value: String
)

private val Theater.apiFields: List<TheaterApiField>
    get() = listOf(
        TheaterApiField(name = "theaCd", label = "영화관 코드", value = id),
        TheaterApiField(name = "theaName", label = "영화관 이름", value = name),
        TheaterApiField(name = "scrnName", label = "상영관 이름", value = screenName),
        TheaterApiField(name = "screenGb", label = "상영관 구분", value = screenType),
        TheaterApiField(name = "address", label = "주소", value = address),
        TheaterApiField(name = "phone", label = "전화번호", value = phone),
        TheaterApiField(name = "homepage", label = "홈페이지", value = homepage),
        TheaterApiField(name = "seatCount", label = "좌석 수", value = seatCount.toString()),
        TheaterApiField(name = "naverMapUrl", label = "네이버 지도 URL", value = naverMapUrl)
    )

@Preview(showBackground = true)
@Composable
private fun TheaterDetailContentPreview() {
    FilmoTheme {
        TheaterDetailContent(
            uiState = TheaterDetailUiState(
                theater = Theater(
                    id = "artnine",
                    name = "아트나인",
                    screenName = "0관",
                    screenType = "예술영화관",
                    address = "서울특별시 동작구 동작대로 89",
                    phone = "02-536-0058",
                    homepage = "https://www.artnine.co.kr",
                    seatCount = 92,
                    naverMapUrl = "https://map.naver.com"
                ),
                isSaved = true
            ),
            theaterId = "artnine",
            onBack = {},
            onRetryClick = {},
            onBookmarkClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TheaterDetailLoadingPreview() {
    FilmoTheme {
        TheaterDetailContent(
            uiState = TheaterDetailUiState(isLoading = true),
            theaterId = "artnine",
            onBack = {},
            onRetryClick = {},
            onBookmarkClick = {}
        )
    }
}
