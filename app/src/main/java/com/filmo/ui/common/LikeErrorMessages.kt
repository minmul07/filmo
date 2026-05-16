package com.filmo.ui.common

import retrofit2.HttpException

internal const val OwnPostLikeNotAllowedMessage = "나의 게시물에는 좋아요를 누를 수 없습니다."
internal const val GenericLikeErrorMessage = "좋아요를 변경하지 못했어요. 다시 시도해 주세요."

internal fun Throwable.toTicketLikeErrorMessage(): String {
    return if (this is HttpException && code() == 400) {
        OwnPostLikeNotAllowedMessage
    } else {
        GenericLikeErrorMessage
    }
}
