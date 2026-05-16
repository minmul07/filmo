package com.filmo.ui.setup

import kotlin.random.Random

data class InitialSetupUiState(
    val nickname: String = generateAnonymousNickname(),
    val step: InitialSetupStep = InitialSetupStep.EntryChoice,
    val isAutomaticNickname: Boolean = false,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
    val hasSaveError: Boolean = false
) {
    val canSave: Boolean
        get() = nickname.isNotBlank() && !isSaving

    val shouldShowRegenerateButton: Boolean
        get() = step == InitialSetupStep.AnonymousProfile && isAutomaticNickname

    val completionMessage: String
        get() = "환영합니다, ${nickname}님"

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

    fun toAutomaticNickname(): InitialSetupUiState {
        return copy(
            nickname = generateAnonymousNickname(),
            step = InitialSetupStep.AnonymousProfile,
            isAutomaticNickname = true,
            hasSaveError = false
        )
    }

    fun toManualNickname(): InitialSetupUiState {
        return copy(
            nickname = "",
            step = InitialSetupStep.AnonymousProfile,
            isAutomaticNickname = false,
            hasSaveError = false
        )
    }

    fun toBackToEntryChoice(): InitialSetupUiState {
        return copy(
            step = InitialSetupStep.EntryChoice,
            isAutomaticNickname = false,
            isSaving = false,
            hasSaveError = false
        )
    }
}

enum class InitialSetupStep {
    EntryChoice,
    AnonymousProfile
}

fun generateAnonymousNickname(
    random: Random = Random.Default
): String {
    val suffix = random.nextInt(from = 0, until = 10_000)
        .toString()
        .padStart(length = 4, padChar = '0')

    return "User$suffix"
}
