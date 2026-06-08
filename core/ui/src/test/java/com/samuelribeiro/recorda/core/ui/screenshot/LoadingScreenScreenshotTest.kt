package com.samuelribeiro.recorda.core.ui.screenshot

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.samuelribeiro.recorda.core.mvi.LoadingUiState
import com.samuelribeiro.recorda.core.ui.LoadingScreen
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LoadingScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingScreen_default() {
        composeRule.setContent {
            LoadingScreen(uiState = LoadingUiState(android.R.string.ok))
        }
        composeRule.onRoot()
            .captureRoboImage("src/test/snapshots/loading_screen_default.png")
    }

    @Test
    fun loadingScreen_customSize() {
        composeRule.setContent {
            LoadingScreen(
                uiState = LoadingUiState(android.R.string.ok),
                indicatorSize = 48.dp,
                spacing = 16.dp,
            )
        }
        composeRule.onRoot()
            .captureRoboImage("src/test/snapshots/loading_screen_custom_size.png")
    }
}
