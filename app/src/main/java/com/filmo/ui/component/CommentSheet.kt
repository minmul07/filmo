package com.filmo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.filmo.ui.theme.FilmoTheme
import com.filmo.ui.theme.LocalFilmoSpacing

@Immutable
data class CommentUiModel(
    val author: String,
    val body: String,
)

fun commentSheetContentDescription(commentCount: Int): String = "댓글 ${commentCount}개"

fun commentItemKey(index: Int, comment: CommentUiModel): String = "${index}-${comment.author}-${comment.body}"

@Composable
fun CommentSheet(
    comments: List<CommentUiModel>,
    modifier: Modifier = Modifier,
    showHomeIndicator: Boolean = true,
) {
    val spacing = LocalFilmoSpacing.current
    val sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = commentSheetContentDescription(comments.size)
            },
        shape = sheetShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "댓글",
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = spacing.md),
            ) {
                if (comments.isEmpty()) {
                    EmptyCommentMessage(
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(
                            start = spacing.lg,
                            end = spacing.lg,
                            bottom = spacing.md,
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        itemsIndexed(
                            items = comments,
                            key = { index, comment -> commentItemKey(index = index, comment = comment) },
                            contentType = { _, _ -> "comment" },
                        ) { _, comment ->
                            CommentListItem(comment = comment)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 3.dp)
                            .width(2.dp)
                            .height(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(1.dp),
                            ),
                    )
                }
            }

            if (showHomeIndicator) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(width = 134.dp, height = 5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onSurface),
                )
            }
        }
    }
}

@Composable
private fun CommentListItem(
    comment: CommentUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = comment.author,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = comment.body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun EmptyCommentMessage(
    modifier: Modifier = Modifier,
) {
    Text(
        text = "아직 댓글이 없어요",
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 350)
@Composable
private fun CommentSheetPreview() {
    FilmoTheme {
        CommentSheet(
            comments = listOf(
                CommentUiModel(
                    author = "독립영화1234",
                    body = "너무 좋았습니다. 시간 가는 줄 몰랐으나 엄청 긴 시간이 영화 같았어요.",
                ),
                CommentUiModel(author = "독립영화1234", body = "잊혀지지 않는 마지막 표정"),
                CommentUiModel(author = "독립영화1234", body = "잊혀지지 않는 마지막 표정"),
                CommentUiModel(author = "독립영화1234", body = "잊혀지지 않는 마지막 표정"),
                CommentUiModel(author = "독립영화1234", body = "잊혀지지 않는 마지막 표정"),
            ),
            modifier = Modifier.height(350.dp),
        )
    }
}
