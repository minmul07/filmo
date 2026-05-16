package com.filmo.ui

import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Test

class AppDestinationTest {
    @Test
    fun initialSetupCompletionReplacesRootBackStackWithMain() {
        val backStack = mutableListOf<NavKey>(AppDestination.InitialSetup)

        backStack.replaceWithMain()

        assertEquals(listOf(AppDestination.Main), backStack)
    }
}
