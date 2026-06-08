package com.samuelribeiro.recorda.core.ui

import androidx.compose.material.Text
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.samuelribeiro.recorda.core.mvi.LoadingUiState
import com.samuelribeiro.recorda.core.mvi.ProcessUiState
import com.samuelribeiro.recorda.core.mvi.ScreenUiState
import org.junit.Rule
import org.junit.Test

private object TestScreenUiState : ScreenUiState {
    override val titleRes = 0
}

class ProcessContainerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenLoadingIsNotNull_loadingScreenIsDisplayed() {
        val uiState = ProcessUiState(
            content = TestScreenUiState,
            loading = LoadingUiState(android.R.string.ok),
        )

        composeTestRule.setContent {
            ProcessContainer(uiState = uiState) { Text("Main Content") }
        }

        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Main Content").assertCountEquals(0)
    }

    @Test
    fun whenLoadingIsNull_mainContentIsDisplayed() {
        val uiState = ProcessUiState<TestScreenUiState>(
            content = TestScreenUiState,
            loading = null,
        )

        composeTestRule.setContent {
            ProcessContainer(uiState = uiState) { Text("Main Content") }
        }

        composeTestRule.onNodeWithText("Main Content").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("OK").assertCountEquals(0)
    }
}
