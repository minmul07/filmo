package com.filmo.ui.collection

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filmo.service.MovieTicket
import com.filmo.ui.theme.FilmoTheme
import kotlinx.coroutines.launch

@Composable
fun CollectionScreen(
    viewModel: CollectionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 0.dp,
    onTicketClick: (String) -> Unit = {},
    onEditTicket: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.loadTickets()
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.dismissToastMessage()
        }
    }

    CollectionContent(
        uiState = uiState,
        modifier = modifier,
        bottomContentPadding = bottomContentPadding,
        onRetryClick = {
            coroutineScope.launch {
                viewModel.loadTickets()
            }
        },
        onTicketClick = onTicketClick,
        onTicketLikeClick = { ticketId ->
            coroutineScope.launch {
                viewModel.toggleTicketLike(ticketId)
            }
        },
        onDismissDelete = viewModel::dismissDeleteDialog,
        onConfirmDelete = {
            coroutineScope.launch {
                viewModel.confirmDeleteTicket()
            }
        }
    )
}

@Composable
fun CollectionDetailScreen(
    ticketId: String,
    viewModel: CollectionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onEditTicket: (String) -> Unit = {},
    onDeleted: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(viewModel, ticketId) {
        viewModel.loadTicketDetail(ticketId)
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.dismissToastMessage()
        }
    }

    CollectionDetailContent(
        ticket = uiState.findTicket(ticketId),
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        pendingDeleteTicket = uiState.pendingDeleteTicket,
        modifier = modifier,
        onBack = onBack,
        onTicketLikeClick = { likedTicketId ->
            coroutineScope.launch {
                viewModel.toggleTicketLike(likedTicketId)
            }
        },
        onDeleteTicket = { viewModel.requestDeleteTicket(ticketId) },
        onDismissDelete = viewModel::dismissDeleteDialog,
        onConfirmDelete = {
            coroutineScope.launch {
                viewModel.confirmDeleteTicket()
                if (viewModel.uiState.value.pendingDeleteTicket == null &&
                    viewModel.uiState.value.errorMessage == null
                ) {
                    onDeleted()
                }
            }
        }
    )
}

@Composable
fun CollectionEditScreen(
    ticketId: String,
    viewModel: CollectionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel, ticketId) {
        viewModel.loadTickets()
        viewModel.startEditingTicket(ticketId)
    }

    CollectionEditContent(
        editingTicket = uiState.editingTicket,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        modifier = modifier,
        onBack = onBack,
        onTheaterNameChange = viewModel::updateEditingTheaterName,
        onWatchedDateChange = viewModel::updateEditingWatchedDate,
        onRatingChange = viewModel::updateEditingRating,
        onReviewChange = viewModel::updateEditingReview,
        onCancel = {
            viewModel.cancelEditingTicket()
            onBack()
        },
        onSave = {
            coroutineScope.launch {
                viewModel.saveEditedTicket()
                if (viewModel.uiState.value.editingTicket == null &&
                    viewModel.uiState.value.errorMessage == null
                ) {
                    onSaved()
                }
            }
        }
    )
}

@Composable
private fun CollectionContent(
    uiState: CollectionUiState,
    modifier: Modifier = Modifier,
    bottomContentPadding: Dp = 0.dp,
    onRetryClick: () -> Unit,
    onTicketClick: (String) -> Unit,
    onTicketLikeClick: (String) -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val tickets = uiState.myTickets

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CollectionTopBar()

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 24.dp + bottomContentPadding
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.errorMessage != null && tickets.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            when {
                uiState.isLoading -> item(span = { GridItemSpan(maxLineSpan) }) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                uiState.errorMessage != null && tickets.isEmpty() -> item(
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    CollectionPlaceholder(
                        title = "티켓을 불러오지 못했어요",
                        body = "잠시 후 다시 시도해 주세요.",
                        action = {
                            OutlinedButton(onClick = onRetryClick) {
                                Text("다시 시도")
                            }
                        }
                    )
                }

                tickets.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                    CollectionPlaceholder(
                        title = "아직 기록한 티켓이 없어요",
                        body = "좋아하는 독립영화를 티켓으로 모아보세요."
                    )
                }

                else -> items(
                    items = tickets,
                    key = { it.id },
                    contentType = { "collection-ticket" }
                ) { ticket ->
                    CollectionTicketCard(
                        ticket = ticket,
                        onClick = { onTicketClick(ticket.id) },
                        onLikeClick = { onTicketLikeClick(ticket.id) }
                    )
                }
            }
        }
    }

    DeleteTicketDialog(
        ticket = uiState.pendingDeleteTicket,
        onDismiss = onDismissDelete,
        onConfirm = onConfirmDelete
    )
}

@Composable
private fun CollectionDetailContent(
    ticket: MovieTicket?,
    isLoading: Boolean,
    errorMessage: String?,
    pendingDeleteTicket: MovieTicket?,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onTicketLikeClick: (String) -> Unit,
    onDeleteTicket: () -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CollectionDetailTopBar(
            title = "티켓 상세",
            showDeleteAction = ticket != null,
            onBack = onBack,
            onDeleteClick = onDeleteTicket
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorMessage != null && ticket != null) {
                item {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            when {
                isLoading -> item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                ticket != null -> item {
                    CollectionDetailTicketCard(
                        ticket = ticket,
                        onLikeClick = { onTicketLikeClick(ticket.id) }
                    )
                }

                else -> item {
                    CollectionPlaceholder(
                        title = "티켓을 찾지 못했어요",
                        body = errorMessage ?: "삭제되었거나 더 이상 접근할 수 없는 티켓입니다."
                    )
                }
            }
        }
    }

    DeleteTicketDialog(
        ticket = pendingDeleteTicket,
        onDismiss = onDismissDelete,
        onConfirm = onConfirmDelete
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionEditContent(
    editingTicket: EditingTicket?,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onTheaterNameChange: (String) -> Unit,
    onWatchedDateChange: (String) -> Unit,
    onRatingChange: (Int) -> Unit,
    onReviewChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        if (isLoading || editingTicket == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                BackTextHeader(title = "티켓 수정", onBack = onBack)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 24.dp,
                top = 28.dp,
                end = 24.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                BackTextHeader(title = "티켓 수정", onBack = onBack)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = editingTicket.movieTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = editingTicket.theaterName,
                    onValueChange = onTheaterNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("영화관") },
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = editingTicket.watchedDate,
                    onValueChange = onWatchedDateChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("관람일") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "관람일"
                        )
                    },
                    singleLine = true
                )
            }

            item {
                Text(
                    text = "별점",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                RatingEditor(
                    rating = editingTicket.rating,
                    onRatingChange = onRatingChange
                )
            }

            item {
                OutlinedTextField(
                    value = editingTicket.review,
                    onValueChange = onReviewChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(178.dp),
                    label = { Text("관람 후기") },
                    minLines = 5
                )
            }

            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("저장")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CollectionContentPreview() {
    FilmoTheme {
        CollectionContent(
            uiState = CollectionUiState(
                myTickets = listOf(
                    MovieTicket(
                        id = "preview-1",
                        movieTitle = "과속스캔들",
                        theaterName = "서울아트시네마",
                        watchedDate = "2024-03-20",
                        rating = 4,
                        review = "유쾌하고 따뜻한 영화였어요",
                        ownedByMe = true,
                        savedByMe = false
                    )
                )
            ),
            onRetryClick = {},
            onTicketClick = {},
            onTicketLikeClick = {},
            onDismissDelete = {},
            onConfirmDelete = {}
        )
    }
}
