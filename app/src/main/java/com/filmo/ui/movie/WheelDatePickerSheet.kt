package com.filmo.ui.movie

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filmo.ui.theme.FilmoGray200
import com.filmo.ui.theme.FilmoPrimary200
import com.filmo.ui.theme.FilmoPrimary400
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import kotlin.math.abs

internal data class WheelDateSelection(
    val year: Int,
    val month: Int,
    val day: Int
) {
    fun withYear(year: Int): WheelDateSelection {
        return copy(year = year).coerceValidDay()
    }

    fun withMonth(month: Int): WheelDateSelection {
        return copy(month = month).coerceValidDay()
    }

    fun withDay(day: Int): WheelDateSelection {
        return copy(day = day).coerceValidDay()
    }

    fun toStartOfDayMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return LocalDate.of(year, month, day)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    private fun coerceValidDay(): WheelDateSelection {
        val lastDay = YearMonth.of(year, month).lengthOfMonth()
        return copy(day = day.coerceIn(1, lastDay))
    }
}

internal fun Long.toWheelDateSelection(zoneId: ZoneId = ZoneId.systemDefault()): WheelDateSelection {
    val localDate = Instant.ofEpochMilli(this)
        .atZone(zoneId)
        .toLocalDate()

    return WheelDateSelection(
        year = localDate.year,
        month = localDate.monthValue,
        day = localDate.dayOfMonth
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WheelDatePickerSheet(
    selection: WheelDateSelection,
    onSelectionChange: (WheelDateSelection) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    yearRange: IntRange = DefaultWheelYearRange()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val effectiveYearRange = minOf(yearRange.first, selection.year)..maxOf(yearRange.last, selection.year)
    val days = (1..YearMonth.of(selection.year, selection.month).lengthOfMonth()).toList()

    LaunchedEffect(selection.year, selection.month) {
        if (selection.day !in days) {
            onSelectionChange(selection.withDay(selection.day))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 28.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "관람일 선택",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                WheelPickerColumn(
                    values = effectiveYearRange.toList(),
                    selectedValue = selection.year,
                    label = { it.toString() },
                    onValueSelected = { onSelectionChange(selection.withYear(it)) },
                    modifier = Modifier.weight(1f)
                )
                WheelPickerColumn(
                    values = (1..12).toList(),
                    selectedValue = selection.month,
                    label = { it.toString().padStart(2, '0') },
                    onValueSelected = { onSelectionChange(selection.withMonth(it)) },
                    modifier = Modifier.weight(1f)
                )
                WheelPickerColumn(
                    values = days,
                    selectedValue = selection.day.coerceAtMost(days.last()),
                    label = { it.toString().padStart(2, '0') },
                    onValueSelected = { onSelectionChange(selection.withDay(it)) },
                    modifier = Modifier.weight(1f)
                )
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "완료",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun WheelPickerColumn(
    values: List<Int>,
    selectedValue: Int,
    label: (Int) -> String,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = values.indexOf(selectedValue).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val itemHeight = 30.dp
    val wheelHeight = itemHeight * VisibleWheelItemCount
    val edgePadding = itemHeight * ((VisibleWheelItemCount - 1) / 2)
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

    LaunchedEffect(values, selectedIndex) {
        if (!listState.isScrollInProgress && listState.firstVisibleItemIndex != selectedIndex) {
            listState.scrollToItem(selectedIndex)
        }
    }

    LaunchedEffect(listState, values, itemHeightPx) {
        snapshotFlow {
            val offsetIndex = if (listState.firstVisibleItemScrollOffset >= itemHeightPx / 2f) 1 else 0
            (listState.firstVisibleItemIndex + offsetIndex).coerceIn(values.indices)
        }
            .distinctUntilChanged()
            .collect { index ->
                onValueSelected(values[index])
            }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val targetIndex = (
                listState.firstVisibleItemIndex +
                    if (listState.firstVisibleItemScrollOffset >= itemHeightPx / 2f) 1 else 0
                ).coerceIn(values.indices)
            listState.animateScrollToItem(targetIndex)
        }
    }

    Box(
        modifier = modifier.height(wheelHeight),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = edgePadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(
                items = values,
                key = { _, value -> value }
            ) { index, value ->
                val distanceFromSelected = abs(index - selectedIndex)
                Text(
                    text = label(value),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = when (distanceFromSelected) {
                            0 -> 20.sp
                            1 -> 18.sp
                            else -> 14.sp
                        },
                        lineHeight = 22.sp,
                        fontWeight = if (distanceFromSelected == 0) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Medium
                        }
                    ),
                    color = when (distanceFromSelected) {
                        0 -> MaterialTheme.colorScheme.onSurface
                        1 -> FilmoPrimary400
                        else -> FilmoPrimary200
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
        WheelSelectionFrame(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight + 2.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun WheelSelectionFrame(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        HorizontalDivider(color = FilmoGray200)
        Box(modifier = Modifier.weight(1f))
        HorizontalDivider(color = FilmoGray200)
    }
}

private fun DefaultWheelYearRange(): IntRange {
    val currentYear = LocalDate.now().year
    return (currentYear - PastWheelYearCount)..(currentYear + FutureWheelYearCount)
}

private const val VisibleWheelItemCount = 5
private const val PastWheelYearCount = 80
private const val FutureWheelYearCount = 1
