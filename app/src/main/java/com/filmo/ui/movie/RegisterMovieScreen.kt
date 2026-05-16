package com.filmo.ui.movie

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.filmo.ui.theme.FilmoTheme
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RegisterMovieScreen(
    viewModel: RegisterMovieViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateToCollection: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        val handledByStep = viewModel.goBack()
        if (!handledByStep) {
            onBack()
        }
    }

    RegisterMovieContent(
        uiState = uiState,
        modifier = modifier,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onTitleChange = viewModel::updateTitle,
        onReleaseDateChange = viewModel::updateReleaseDateMillis,
        onGenreChange = viewModel::updateGenre,
        onDirectorChange = viewModel::updateDirector,
        onCastChange = viewModel::updateCast,
        onTemplateClick = viewModel::selectTicketTemplate,
        onNext = viewModel::goToNextStep,
        onBack = {
            val handledByStep = viewModel.goBack()
            if (!handledByStep) {
                onBack()
            }
        },
        onPublishComplete = viewModel::markPublishingComplete,
        onCollectionClick = {
            viewModel.reset()
            onNavigateToCollection()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterMovieContent(
    uiState: RegisterMovieUiState,
    modifier: Modifier = Modifier,
    onSearchQueryChange: (String) -> Unit = {},
    onTitleChange: (String) -> Unit = {},
    onReleaseDateChange: (Long?) -> Unit = {},
    onGenreChange: (String) -> Unit = {},
    onDirectorChange: (String) -> Unit = {},
    onCastChange: (String) -> Unit = {},
    onTemplateClick: (TicketTemplateOption) -> Unit = {},
    onNext: () -> Unit = {},
    onBack: () -> Unit = {},
    onPublishComplete: () -> Unit = {},
    onCollectionClick: () -> Unit = {}
) {
    var isDatePickerOpen by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.releaseDateMillis,
        selectableDates = rememberPastOrTodaySelectableDates()
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            RegisterMovieHeader(
                step = uiState.step,
                isPublishComplete = uiState.isPublishComplete,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState.step) {
                RegisterMovieStep.MovieSearch -> MovieSearchStep(
                    query = uiState.searchQuery,
                    errorMessage = uiState.errorMessage,
                    onQueryChange = onSearchQueryChange,
                    onNext = onNext
                )

                RegisterMovieStep.MovieInfo -> MovieInfoStep(
                    uiState = uiState,
                    errorMessage = uiState.errorMessage,
                    onTitleChange = onTitleChange,
                    onReleaseDateClick = { isDatePickerOpen = true },
                    onGenreChange = onGenreChange,
                    onDirectorChange = onDirectorChange,
                    onCastChange = onCastChange,
                    onNext = onNext
                )

                RegisterMovieStep.TicketTemplate -> TicketTemplateStep(
                    selectedTemplate = uiState.selectedTicketTemplate,
                    errorMessage = uiState.errorMessage,
                    onTemplateClick = onTemplateClick,
                    onNext = onNext
                )

                RegisterMovieStep.Publishing -> PublishingStep(
                    uiState = uiState,
                    onPublishComplete = onPublishComplete,
                    onCollectionClick = onCollectionClick
                )
            }
        }
    }

    if (isDatePickerOpen) {
        DatePickerDialog(
            onDismissRequest = { isDatePickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReleaseDateChange(datePickerState.selectedDateMillis)
                        isDatePickerOpen = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerOpen = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun RegisterMovieHeader(
    step: RegisterMovieStep,
    isPublishComplete: Boolean,
    onBack: () -> Unit
) {
    if (step == RegisterMovieStep.Publishing) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isPublishComplete) "티켓 발행 완료!" else "티켓 발행 중",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기"
            )
        }
        Text(
            text = step.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${step.index}/4",
            modifier = Modifier.width(48.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MovieSearchStep(
    query: String,
    errorMessage: String?,
    onQueryChange: (String) -> Unit,
    onNext: () -> Unit
) {
    StepContent(
        action = {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("다음")
            }
        }
    ) {
        Text(
            text = "영화 검색",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "백엔드 검색 API가 연결되면 선택한 영화의 제목, 장르, 감독, 출연 정보가 자동으로 입력됩니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null
                )
            },
            label = { Text("영화 제목 검색") },
            singleLine = true
        )
        PlaceholderPanel(
            title = "검색 결과 준비 중",
            body = "TODO: 영화 검색 API 연결 후 결과 리스트를 표시합니다."
        )
        ErrorText(errorMessage = errorMessage)
    }
}

@Composable
private fun MovieInfoStep(
    uiState: RegisterMovieUiState,
    errorMessage: String?,
    onTitleChange: (String) -> Unit,
    onReleaseDateClick: () -> Unit,
    onGenreChange: (String) -> Unit,
    onDirectorChange: (String) -> Unit,
    onCastChange: (String) -> Unit,
    onNext: () -> Unit
) {
    StepContent(
        action = {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("티켓 디자인 선택")
            }
        }
    ) {
        Text(
            text = "영화 정보 입력",
            style = MaterialTheme.typography.headlineSmall
        )
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("영화 제목 *") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.releaseDateMillis.toDateText(),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("관람 날짜 *") },
            readOnly = true,
            singleLine = true,
            supportingText = { Text("미래 날짜는 선택할 수 없어요.") }
        )
        OutlinedButton(
            onClick = onReleaseDateClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("날짜 선택")
        }
        OutlinedTextField(
            value = uiState.genre,
            onValueChange = onGenreChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("장르 *") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.director,
            onValueChange = onDirectorChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("감독") },
            singleLine = true
        )
        OutlinedTextField(
            value = uiState.cast,
            onValueChange = onCastChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("출연") },
            minLines = 2
        )
        ErrorText(errorMessage = errorMessage)
    }
}

@Composable
private fun TicketTemplateStep(
    selectedTemplate: TicketTemplateOption?,
    errorMessage: String?,
    onTemplateClick: (TicketTemplateOption) -> Unit,
    onNext: () -> Unit
) {
    StepContent(
        action = {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("티켓 발행하기")
            }
        }
    ) {
        Text(
            text = "티켓 디자인 선택",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "완성된 티켓 디자인이 준비되면 이 placeholder 카드들이 실제 디자인으로 교체됩니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TicketTemplateOption.entries.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { option ->
                        TicketTemplateCard(
                            option = option,
                            selected = option == selectedTemplate,
                            onClick = { onTemplateClick(option) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        ErrorText(errorMessage = errorMessage)
    }
}

@Composable
private fun TicketTemplateCard(
    option: TicketTemplateOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = modifier
            .aspectRatio(0.72f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Movie,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(100))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "선택됨",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PublishingStep(
    uiState: RegisterMovieUiState,
    onPublishComplete: () -> Unit,
    onCollectionClick: () -> Unit
) {
    var progress by remember(
        uiState.publishStartedAtMillis,
        uiState.publishingStatus
    ) {
        mutableIntStateOf(
            if (uiState.isPublishComplete) {
                100
            } else {
                publishingProgressPercent(
                    startedAtMillis = uiState.publishStartedAtMillis,
                    nowMillis = System.currentTimeMillis()
                )
            }
        )
    }

    LaunchedEffect(uiState.publishStartedAtMillis, uiState.publishingStatus) {
        if (uiState.publishingStatus != PublishingStatus.Publishing) {
            progress = 100
            return@LaunchedEffect
        }

        while (true) {
            val currentProgress = publishingProgressPercent(
                startedAtMillis = uiState.publishStartedAtMillis,
                nowMillis = System.currentTimeMillis()
            )
            progress = currentProgress
            if (currentProgress >= 100) {
                onPublishComplete()
                break
            }
            delay(32L)
        }
    }

    val cardScale by animateFloatAsState(
        targetValue = if (uiState.isPublishComplete) 1.03f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "ticket_complete_scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TicketPreview(
                uiState = uiState,
                modifier = Modifier.graphicsLayer {
                    scaleX = cardScale
                    scaleY = cardScale
                }
            )
            Spacer(modifier = Modifier.height(28.dp))
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "$progress%",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.titleMedium
            )
            AnimatedVisibility(
                visible = uiState.isPublishComplete,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "티켓 발행 완료",
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.isPublishComplete,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("피드 공유")
                    }
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = "이미지 저장"
                        )
                    }
                }
                Button(
                    onClick = onCollectionClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("내 컬렉션으로 이동")
                }
            }
        }
    }
}

@Composable
private fun TicketPreview(
    uiState: RegisterMovieUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.65f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FILMO TICKET",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = uiState.selectedTicketTemplate?.label.orEmpty(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = uiState.title.ifBlank { "영화 제목" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = uiState.genre.ifBlank { "장르" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = uiState.releaseDateMillis.toDateText().ifBlank { "관람 날짜" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun StepContent(
    action: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
        Spacer(modifier = Modifier.height(16.dp))
        action()
    }
}

@Composable
private fun PlaceholderPanel(
    title: String,
    body: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Movie,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = body,
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorText(errorMessage: String?) {
    if (errorMessage == null) return

    Text(
        text = errorMessage,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberPastOrTodaySelectableDates(): SelectableDates {
    val todayEndMillis = remember {
        LocalDate.now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli() - 1L
    }

    return remember(todayEndMillis) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= todayEndMillis
            }
        }
    }
}

private val RegisterMovieStep.title: String
    get() = when (this) {
        RegisterMovieStep.MovieSearch -> "영화 검색"
        RegisterMovieStep.MovieInfo -> "영화 정보 입력"
        RegisterMovieStep.TicketTemplate -> "티켓 사진 선택"
        RegisterMovieStep.Publishing -> "티켓 발행"
    }

private val RegisterMovieStep.index: Int
    get() = when (this) {
        RegisterMovieStep.MovieSearch -> 1
        RegisterMovieStep.MovieInfo -> 2
        RegisterMovieStep.TicketTemplate -> 3
        RegisterMovieStep.Publishing -> 4
    }

private fun Long?.toDateText(): String {
    return this?.let { millis ->
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
    }.orEmpty()
}

@Preview(showBackground = true)
@Composable
private fun RegisterMovieContentPreview() {
    FilmoTheme {
        RegisterMovieContent(
            uiState = RegisterMovieUiState(
                step = RegisterMovieStep.MovieInfo,
                title = "괴물",
                releaseDateMillis = 1_609_459_200_000L,
                genre = "드라마",
                director = "봉준호",
                cast = "송강호, 변희봉"
            )
        )
    }
}
