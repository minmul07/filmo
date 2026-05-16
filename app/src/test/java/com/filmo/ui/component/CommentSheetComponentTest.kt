package com.filmo.ui.component

import org.junit.Assert.assertEquals
import org.junit.Test

class CommentSheetComponentTest {
    @Test
    fun commentSheetContentDescriptionIncludesCommentCount() {
        assertEquals("댓글 0개", commentSheetContentDescription(commentCount = 0))
        assertEquals("댓글 5개", commentSheetContentDescription(commentCount = 5))
    }

    @Test
    fun commentItemKeyStaysUniqueWhenCommentTextIsDuplicated() {
        val comment = CommentUiModel(author = "독립영화1234", body = "잊혀지지 않는 마지막 표정")

        assertEquals("0-독립영화1234-잊혀지지 않는 마지막 표정", commentItemKey(index = 0, comment = comment))
        assertEquals("1-독립영화1234-잊혀지지 않는 마지막 표정", commentItemKey(index = 1, comment = comment))
    }
}
