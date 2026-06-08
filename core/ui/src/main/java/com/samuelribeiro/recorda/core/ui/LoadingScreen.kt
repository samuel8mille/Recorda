package com.samuelribeiro.recorda.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.samuelribeiro.recorda.core.mvi.LoadingUiState

/**
 * Full-screen loading composable with a progress indicator and message text.
 *
 * @param uiState Loading state providing the message text.
 * @param indicatorSize Diameter of the [CircularProgressIndicator].
 * @param spacing Vertical space between the indicator and the message text.
 */
@Composable
fun LoadingScreen(
    uiState: LoadingUiState,
    indicatorSize: Dp = 72.dp,
    spacing: Dp = 8.dp,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            Modifier
                .size(indicatorSize)
                .clearAndSetSemantics {}
        )
        Spacer(Modifier.height(spacing))
        Text(text = stringResource(uiState.messageRes))
    }
}
