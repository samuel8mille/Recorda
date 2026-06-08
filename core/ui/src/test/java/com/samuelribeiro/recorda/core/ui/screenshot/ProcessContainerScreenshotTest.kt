package com.samuelribeiro.recorda.core.ui.screenshot

import androidx.compose.material.Text
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.samuelribeiro.recorda.core.mvi.LoadingUiState
import com.samuelribeiro.recorda.core.mvi.ProcessUiState
import com.samuelribeiro.recorda.core.mvi.ScreenUiState
import com.samuelribeiro.recorda.core.ui.ProcessContainer
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

private object TestScreenState : ScreenUiState {
    override val titleRes = 0
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ProcessContainerScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun processContainer_content() {
        composeRule.setContent {
            ProcessContainer(
                uiState = ProcessUiState(content = TestScreenState),
            ) {
                Text("Main Content")
            }
        }
        composeRule.onRoot()
            .captureRoboImage("src/test/snapshots/process_container_content.png")
    }

    @Test
    fun processContainer_loading() {
        composeRule.setContent {
            ProcessContainer(
                uiState = ProcessUiState(
                    content = TestScreenState,
                    loading = LoadingUiState(android.R.string.ok),
                ),
            ) {
                Text("Main Content")
            }
        }
        composeRule.onRoot()
            .captureRoboImage("src/test/snapshots/process_container_loading.png")
    }
}
