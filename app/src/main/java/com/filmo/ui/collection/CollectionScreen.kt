package com.filmo.ui.collection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
    onTicketClick: (String) -> Unit = {},
    onEditTicket: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.loadTickets()
    }

    CollectionContent(
        uiState = uiState,
        modifier = modifier,
        onTabClick = viewModel::selectTab,
        onRetryClick = {
            coroutineScope.launch {
                viewModel.loadTickets()
            }
        },
        onTicketClick = onTicketClick,
        onEditTicket = onEditTicket,
        onDeleteTicket = viewModel::requestDeleteTicket,
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

    LaunchedEffect(viewModel, ticketId) {
        viewModel.loadTickets()
    }

    CollectionDetailContent(
        ticket = uiState.findTicket(ticketId),
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        pendingDeleteTicket = uiState.pendingDeleteTicket,
        modifier = modifier,
        onBack = onBack,
        onEditTicket = { onEditTicket(ticketId) },
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
    onTabClick: (CollectionTicketTab) -> Unit,
    onRetryClick: () -> Unit,
    onTicketClick: (String) -> Unit,
    onEditTicket: (String) -> Unit,
    onDeleteTicket: (String) -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 24.dp,
                top = 32.dp,
                end = 24.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                CollectionHeader()
            }

            item {
                CollectionTabs(
                    selectedTab = uiState.selectedTab,
                    onTabClick = onTabClick
                )
            }

            if (uiState.errorMessage != null && uiState.visibleTickets.isNotEmpty()) {
                item {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            when {
                uiState.isLoading -> item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                uiState.errorMessage != null && uiState.visibleTickets.isEmpty() -> item {
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

                uiState.visibleTickets.isEmpty() -> item {
                    CollectionPlaceholder(
                        title = if (uiState.selectedTab == CollectionTicketTab.MyTickets) {
                            "아직 기록한 티켓이 없어요"
                        } else {
                            "아직 저장한 티켓이 없어요"
                        },
                        body = "좋아하는 독립영화를 티켓으로 모아보세요."
                    )
                }

                else -> items(
                    items = uiState.visibleTickets,
                    key = { it.id }
                ) { ticket ->
                    TicketCard(
                        ticket = ticket,
                        showOwnerActions = uiState.selectedTab == CollectionTicketTab.MyTickets,
                        onClick = { onTicketClick(ticket.id) },
                        onEditClick = { onEditTicket(ticket.id) },
                        onDeleteClick = { onDeleteTicket(ticket.id) }
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
private fun CollectionHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "내 컬렉션",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "내가 기록하고 저장한 영화 티켓",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CollectionTabs(
    selectedTab: CollectionTicketTab,
    onTabClick: (CollectionTicketTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CollectionTicketTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            Button(
                onClick = { onTabClick(tab) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (selected) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(tab.label)
            }
        }
    }
}

@Composable
private fun TicketCard(
    ticket: MovieTicket,
    showOwnerActions: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "MOVIE TICKET",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = ticket.movieTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = ticket.theaterName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = ticket.watchedDate,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                RatingStars(
                    rating = ticket.rating,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = ticket.review,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (showOwnerActions) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onEditClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("수정")
                        }
                        Button(
                            onClick = onDeleteClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                contentColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("삭제")
                        }
                    }
                } else {
                    Button(
                        onClick = onDeleteClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Icon(Icons.Filled.Bookmark, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("저장 취소")
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingStars(
    rating: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (index < rating) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

@Composable
private fun CollectionPlaceholder(
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

@Composable
private fun DeleteTicketDialog(
    ticket: MovieTicket?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (ticket == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        title = {
            Text(
                text = if (ticket.ownedByMe) "티켓 삭제" else "저장 삭제",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = if (ticket.ownedByMe) {
                    "이 티켓을 삭제하시겠습니까?"
                } else {
                    "저장한 티켓에서 삭제하시겠습니까?"
                },
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("삭제")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("취소")
            }
        }
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
    onEditTicket: () -> Unit,
    onDeleteTicket: () -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            BackTextHeader(title = "티켓 상세", onBack = onBack)

            when {
                isLoading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                ticket != null -> TicketCard(
                    ticket = ticket,
                    showOwnerActions = ticket.ownedByMe,
                    onClick = {},
                    onEditClick = onEditTicket,
                    onDeleteClick = onDeleteTicket
                )

                else -> CollectionPlaceholder(
                    title = "티켓을 찾지 못했어요",
                    body = errorMessage ?: "삭제되었거나 더 이상 접근할 수 없는 티켓입니다."
                )
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

@Composable
private fun BackTextHeader(
    title: String,
    onBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(
            onClick = onBack,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text("뒤로가기")
        }
        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun RatingEditor(
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        (1..5).forEach { score ->
            IconButton(onClick = { onRatingChange(score) }) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "$score 점",
                    tint = if (score <= rating) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
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
            onTabClick = {},
            onRetryClick = {},
            onTicketClick = {},
            onEditTicket = {},
            onDeleteTicket = {},
            onDismissDelete = {},
            onConfirmDelete = {}
        )
    }
}
