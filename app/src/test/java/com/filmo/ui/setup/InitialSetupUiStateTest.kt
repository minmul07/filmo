package com.filmo.ui.setup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InitialSetupUiStateTest {
    @Test
    fun generatedNicknameUsesUserPrefixAndFourDigits() {
        val nickname = generateAnonymousNickname()

        assertTrue(nickname.matches(Regex("User\\d{4}")))
    }

    @Test
    fun blankNicknameCannotBeSaved() {
        val state = InitialSetupUiState(nickname = "   ")

        assertFalse(state.canSave)
    }

    @Test
    fun savingStateDisablesSave() {
        val state = InitialSetupUiState(nickname = "User1234").toSaving()

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
    fun initialStateShowsEntryChoice() {
        val state = InitialSetupUiState()

        assertEquals(InitialSetupStep.EntryChoice, state.step)
        assertFalse(state.shouldShowRegenerateButton)
    }

    @Test
    fun automaticNicknameFlowShowsRegenerateButton() {
        val state = InitialSetupUiState().toAutomaticNickname()

        assertEquals(InitialSetupStep.AnonymousProfile, state.step)
        assertTrue(state.shouldShowRegenerateButton)
    }

    @Test
    fun manualNicknameFlowHidesRegenerateButton() {
        val state = InitialSetupUiState().toManualNickname()

        assertEquals(InitialSetupStep.AnonymousProfile, state.step)
        assertEquals("", state.nickname)
        assertFalse(state.shouldShowRegenerateButton)
    }

    @Test
    fun backToEntryChoiceReturnsToInitialPage() {
        val state = InitialSetupUiState()
            .toAutomaticNickname()
            .toBackToEntryChoice()

        assertEquals(InitialSetupStep.EntryChoice, state.step)
        assertFalse(state.shouldShowRegenerateButton)
    }

    @Test
    fun completionMessageIncludesSavedNickname() {
        val state = InitialSetupUiState(nickname = "시네필여행자965")

        assertEquals("환영합니다, 시네필여행자965님", state.completionMessage)
    }
}
