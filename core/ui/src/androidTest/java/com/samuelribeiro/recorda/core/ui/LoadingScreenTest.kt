package com.samuelribeiro.recorda.core.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.samuelribeiro.recorda.core.mvi.LoadingUiState
import org.junit.Rule
import org.junit.Test

class LoadingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenStateIsProvided_displaysCorrectText() {
        composeTestRule.setContent {
            LoadingScreen(uiState = LoadingUiState(android.R.string.ok))
        }

        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
    }
}
