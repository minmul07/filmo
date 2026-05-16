package com.filmo.ui.setup

data class InitialSetupUiState(
    val loginId: String = "",
    val password: String = "",
    val nickname: String = "",
    val step: InitialSetupStep = InitialSetupStep.Login,
    val isAutomaticNickname: Boolean = false,
    val isNicknameLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
    val hasSaveError: Boolean = false,
    val hasNicknameLoadError: Boolean = false
) {
    val isLoginIdValid: Boolean
        get() = loginId.trim().length >= MIN_CREDENTIAL_LENGTH

    val isPasswordValid: Boolean
        get() = password.length >= MIN_CREDENTIAL_LENGTH

    val isNicknameValid: Boolean
        get() = nickname.trim().isNotEmpty()

    val canSave: Boolean
        get() {
            if (isSaving || isNicknameLoading) return false

            return when (step) {
                InitialSetupStep.EntryChoice -> false
                // Signup is intentionally unused in the login-only MVP.
                InitialSetupStep.Signup -> false
                InitialSetupStep.Login -> isLoginIdValid && isPasswordValid
            }
        }

    val shouldShowRegenerateButton: Boolean
        get() = false

    val completionMessage: String
        get() = if (nickname.isBlank()) {
            "로그인되었습니다."
        } else {
            "환영합니다, ${nickname}님"
        }

    fun toLoginIdChanged(loginId: String): InitialSetupUiState {
        return copy(
            loginId = loginId,
            hasSaveError = false
        )
    }

    fun toPasswordChanged(password: String): InitialSetupUiState {
        return copy(
            password = password,
            hasSaveError = false
        )
    }

    fun toNicknameChanged(nickname: String): InitialSetupUiState {
        return copy(
            nickname = nickname,
            hasSaveError = false,
            hasNicknameLoadError = false
        )
    }

    fun toSaving(): InitialSetupUiState {
        return copy(
            loginId = loginId.trim(),
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

    fun toLogin(): InitialSetupUiState {
        return copy(
            nickname = "",
            step = InitialSetupStep.Login,
            isAutomaticNickname = false,
            isNicknameLoading = false,
            hasNicknameLoadError = false,
            hasSaveError = false
        )
    }

    fun toBackToEntryChoice(): InitialSetupUiState {
        // The entry choice page is intentionally unused; returning to setup keeps login visible.
        return copy(
            step = InitialSetupStep.Login,
            isAutomaticNickname = false,
            isNicknameLoading = false,
            isSaving = false,
            hasNicknameLoadError = false,
            hasSaveError = false
        )
    }

    companion object {
        const val MIN_CREDENTIAL_LENGTH = 4
    }
}

enum class InitialSetupStep {
    EntryChoice,
    Signup,
    Login
}
