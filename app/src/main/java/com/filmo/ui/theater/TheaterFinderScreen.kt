package com.filmo.ui.theater

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.LocalMovies
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
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
fun TheaterFinderScreen(
    onTheaterClick: (theaterId: String, initiallySaved: Boolean) -> Unit,
    syncedSavedTheaterIds: Set<String>? = null,
    onBookmarkChanged: (theaterId: String, saved: Boolean) -> Unit = { _, _ -> },
    viewModel: TheaterFinderViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.loadTheaters()
    }

    TheaterFinderContent(
        uiState = if (syncedSavedTheaterIds == null) {
            uiState
        } else {
            uiState.copy(savedTheaterIds = uiState.savedTheaterIds + syncedSavedTheaterIds)
        },
        modifier = modifier,
        onRetryClick = {
            coroutineScope.launch {
                viewModel.loadTheaters()
            }
        },
        onLoadNextTheaters = {
            coroutineScope.launch {
                viewModel.loadNextTheaterPage()
            }
        },
        onTheaterClick = { theater, saved ->
            Timber.d("TheaterFinderScreen.theaterClick theaterId=%s saved=%s", theater.id, saved)
            onTheaterClick(theater.id, saved)
        },
        onSaveClick = { theater, saved ->
            Timber.d("TheaterFinderScreen.bookmarkClick theaterId=%s saved=%s", theater.id, saved)
            coroutineScope.launch {
                val changed = if (saved) {
                    viewModel.removeSavedTheater(theater.id)
                } else {
                    viewModel.saveTheater(theater.id)
                }
                if (changed) {
                    onBookmarkChanged(theater.id, !saved)
                }
            }
        }
    )
}

@Composable
private fun TheaterFinderContent(
    uiState: TheaterFinderUiState,
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit = {},
    onLoadNextTheaters: () -> Unit = {},
    onTheaterClick: (Theater, Boolean) -> Unit = { _, _ -> },
    onSaveClick: (Theater, Boolean) -> Unit = { _, _ -> }
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            totalItems > 0 && lastVisibleIndex >= totalItems - TheaterPrefetchThreshold
        }
    }

    LaunchedEffect(
        shouldLoadMore,
        uiState.canLoadMore,
        uiState.isLoading,
        uiState.isAppending,
        uiState.theaters.size
    ) {
        if (shouldLoadMore && uiState.canLoadMore && !uiState.isLoading && !uiState.isAppending) {
            onLoadNextTheaters()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                top = 32.dp,
                end = 24.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item(
                key = "theater_finder_header",
                contentType = "header"
            ) {
                Text(
                    text = "독립 영화관 목록",
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (uiState.errorMessage != null && uiState.theaters.isNotEmpty()) {
                item(
                    key = "theater_finder_inline_error",
                    contentType = "inline_error"
                ) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (uiState.bookmarkErrorMessage != null) {
                item(
                    key = "theater_finder_bookmark_error",
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
                uiState.isLoading -> item(
                    key = "theater_finder_loading",
                    contentType = "loading"
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                uiState.errorMessage != null && uiState.theaters.isEmpty() -> item(
                    key = "theater_finder_error",
                    contentType = "placeholder"
                ) {
                    TheaterPlaceholder(
                        title = "영화관 목록을 불러오지 못했어요",
                        body = "잠시 후 다시 시도해 주세요.",
                        action = {
                            OutlinedButton(onClick = onRetryClick) {
                                Text("다시 시도")
                            }
                        }
                    )
                }

                uiState.theaters.isEmpty() -> item(
                    key = "theater_finder_empty",
                    contentType = "placeholder"
                ) {
                    TheaterPlaceholder(
                        title = "등록된 영화관이 없어요",
                        body = "독립영화관 정보가 추가되면 이곳에서 확인할 수 있어요."
                    )
                }

                else -> {
                    items(
                        items = uiState.theaters,
                        key = { it.listKey },
                        contentType = { "theater" }
                    ) { theater ->
                        val saved = theater.id in uiState.savedTheaterIds
                        TheaterListItem(
                            theater = theater,
                            saved = saved,
                            bookmarkUpdating = theater.id in uiState.updatingBookmarkTheaterIds,
                            onClick = { onTheaterClick(theater, saved) },
                            onSaveClick = { onSaveClick(theater, saved) }
                        )
                    }

                    item(
                        key = "theater_finder_footer",
                        contentType = "footer"
                    ) {
                        TheaterListFooter(
                            isAppending = uiState.isAppending,
                            errorMessage = uiState.errorMessage,
                            canLoadMore = uiState.canLoadMore,
                            onRetryClick = onLoadNextTheaters
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TheaterListItem(
    theater: Theater,
    saved: Boolean,
    bookmarkUpdating: Boolean,
    onClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable(
                role = Role.Button,
                onClick = onClick
            ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = theater.name.ifBlank { "이름 없는 영화관" },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = theater.address.ifBlank { theater.screenName.ifBlank { "주소 정보 없음" } },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = onSaveClick,
            modifier = Modifier.size(44.dp),
            enabled = !bookmarkUpdating
        ) {
            Icon(
                imageVector = if (saved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = if (saved) "저장 취소" else "영화관 저장",
                tint = if (saved) {
                    MaterialTheme.colorScheme.primary
                } else if (bookmarkUpdating) {
                    MaterialTheme.colorScheme.outlineVariant
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

@Composable
private fun TheaterListFooter(
    isAppending: Boolean,
    errorMessage: String?,
    canLoadMore: Boolean,
    onRetryClick: () -> Unit
) {
    when {
        isAppending -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        errorMessage != null -> OutlinedButton(
            onClick = onRetryClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("더 불러오기")
        }

        canLoadMore -> Spacer(modifier = Modifier.height(0.dp))
    }
}

@Composable
private fun TheaterPlaceholder(
    title: String,
    body: String,
    action: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = body,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (action != null) {
                Spacer(modifier = Modifier.height(18.dp))
                action()
            }
        }
    }
}

private val Theater.listKey: String
    get() = id.ifBlank { "$name-$address-$screenName" }

private const val TheaterPrefetchThreshold = 5

@Preview(showBackground = true)
@Composable
private fun TheaterFinderContentPreview() {
    FilmoTheme {
        TheaterFinderContent(
            uiState = TheaterFinderUiState(
                theaters = PreviewTheaters,
                savedTheaterIds = setOf("artnine"),
                canLoadMore = false
            )
        )
    }
}

private val PreviewTheaters = listOf(
    Theater(
        id = "artnine",
        name = "아트나인",
        screenName = "0관",
        screenType = "예술영화관",
        address = "사당동 147-53",
        phone = "02-536-0058",
        homepage = "https://www.artnine.co.kr",
        seatCount = 92,
        naverMapUrl = "https://map.naver.com"
    ),
    Theater(
        id = "indiespace",
        name = "인디스페이스",
        screenName = "1관",
        screenType = "독립영화전용관",
        address = "서울특별시 마포구 양화로 176",
        phone = "02-738-0366",
        homepage = "https://indiespace.kr",
        seatCount = 210,
        naverMapUrl = "https://map.naver.com"
    )
)
