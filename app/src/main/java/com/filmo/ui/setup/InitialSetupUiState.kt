package com.filmo.ui.setup

import kotlin.random.Random

data class InitialSetupUiState(
    val nickname: String = generateAnonymousNickname(),
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
    val hasSaveError: Boolean = false
) {
    val canSave: Boolean
        get() = nickname.isNotBlank() && !isSaving

    fun toNicknameChanged(nickname: String): InitialSetupUiState {
        return copy(
            nickname = nickname,
            hasSaveError = false
        )
    }

    fun toSaving(): InitialSetupUiState {
        return copy(
            nickname = nickname.trim(),
            isSaving = true,
            hasSaveError = false
        )
    }

    fun toSaved(): InitialSetupUiState {
        return copy(
            isSaving = false,
            isCompleted = true,
            hasSaveError = false
        )
    }

    fun toSaveFailure(): InitialSetupUiState {
        return copy(
            isSaving = false,
            hasSaveError = true
        )
    }
}

fun generateAnonymousNickname(
    random: Random = Random.Default
): String {
    val suffix = random.nextInt(from = 0, until = 10_000)
        .toString()
        .padStart(length = 4, padChar = '0')

    return "User$suffix"
}
