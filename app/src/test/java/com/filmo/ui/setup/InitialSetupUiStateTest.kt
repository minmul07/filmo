package com.filmo.ui.setup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InitialSetupUiStateTest {
    @Test
    fun initialStateShowsLoginWithEmptyForm() {
        val state = InitialSetupUiState()

        assertEquals(InitialSetupStep.Login, state.step)
        assertEquals("", state.loginId)
        assertEquals("", state.password)
        assertEquals("", state.nickname)
        assertFalse(state.canSave)
    }

    @Test
    fun signupIsDisabledForLoginOnlyMvp() {
        val state = InitialSetupUiState(
            loginId = "user",
            password = "1234",
            nickname = "시네필",
            step = InitialSetupStep.Signup
        )

        assertFalse(state.canSave)
    }

    @Test
    fun loginRequiresLoginIdAndPassword() {
        val state = InitialSetupUiState()
            .toLogin()
            .toLoginIdChanged("usr")
            .toPasswordChanged("123")

        assertFalse(state.canSave)

        val validState = state
            .toLoginIdChanged("user")
            .toPasswordChanged("1234")

        assertTrue(validState.canSave)
    }

    @Test
    fun savingStateDisablesSave() {
        val state = InitialSetupUiState(
            loginId = "user",
            password = "1234",
            nickname = "User1234",
            step = InitialSetupStep.Login
        ).toSaving()

        assertFalse(state.canSave)
        assertTrue(state.isSaving)
    }

    @Test
    fun saveCompletionKeepsNicknameAndMarksCompleted() {
        val state = InitialSetupUiState(nickname = "CustomName")

        val result = state.toSaved()

        assertEquals("CustomName", result.nickname)
        assertFalse(result.isSaving)
        assertTrue(result.isCompleted)
    }

    @Test
    fun automaticNicknameFlowStaysOnLoginForLoginOnlyMvp() {
        val state = InitialSetupUiState()
            .toAutomaticNicknameLoading()
            .toAutomaticNicknameLoaded("서버닉네임")

        assertEquals(InitialSetupStep.Login, state.step)
        assertEquals("", state.nickname)
        assertFalse(state.shouldShowRegenerateButton)
    }

    @Test
    fun manualNicknameFlowStaysOnLoginForLoginOnlyMvp() {
        val state = InitialSetupUiState().toManualNickname()

        assertEquals(InitialSetupStep.Login, state.step)
        assertEquals("", state.nickname)
        assertFalse(state.shouldShowRegenerateButton)
    }

    @Test
    fun backToEntryChoiceKeepsLoginVisibleForLoginOnlyMvp() {
        val state = InitialSetupUiState()
            .toAutomaticNicknameLoading()
            .toBackToEntryChoice()

        assertEquals(InitialSetupStep.Login, state.step)
        assertFalse(state.shouldShowRegenerateButton)
    }

    @Test
    fun completionMessageIncludesSavedNickname() {
        val state = InitialSetupUiState(nickname = "시네필여행자965")

        assertEquals("환영합니다, 시네필여행자965님", state.completionMessage)
    }
}
