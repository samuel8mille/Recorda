package com.samuelribeiro.recorda.feature.study

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.core.mvi.ProcessUiState
import com.samuelribeiro.recorda.core.ui.ProcessContainer
import com.samuelribeiro.recorda.domain.model.StudySection
import com.samuelribeiro.recorda.presentation.ui.study.CloseSection
import com.samuelribeiro.recorda.presentation.ui.study.SelectSection
import com.samuelribeiro.recorda.presentation.ui.study.StudyUiState
import com.samuelribeiro.recorda.presentation.ui.study.StudyViewModel
import com.samuelribeiro.recorda.ui.theme.HorizontalPadding
import com.samuelribeiro.recorda.ui.theme.SpaceMedium
import com.samuelribeiro.recorda.ui.theme.SpaceSmall

const val STUDY_SECTION_CARD_TEST_TAG = "StudySectionCardTestTag"

@Composable
fun StudyScreen(viewModel: StudyViewModel, onNavigateBack: () -> Unit) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()
    BackHandler(enabled = uiState.content.selectedSectionId != null) {
        viewModel.onSendEvent(CloseSection)
    }
    StudyContent(
        uiState = uiState,
        onSelectSection = { sectionId -> viewModel.onSendEvent(SelectSection(sectionId)) },
        onCloseSection = { viewModel.onSendEvent(CloseSection) },
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StudyContent(
    uiState: ProcessUiState<StudyUiState>,
    onSelectSection: (String) -> Unit,
    onCloseSection: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val selectedSection = uiState.content.selectedSection
    Scaffold(
        topBar = {
            StudyTopBar(
                topicName = uiState.content.topicName,
                isDetailOpen = selectedSection != null,
                onCloseSection = onCloseSection,
                onNavigateBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            ProcessContainer(uiState = uiState) {
                when {
                    selectedSection != null -> StudySectionDetail(section = selectedSection)
                    uiState.content.guide != null -> StudySectionList(
                        sections = uiState.content.guide?.sections.orEmpty(),
                        onSelectSection = onSelectSection,
                    )
                    uiState.error != null -> ErrorMessage()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudyTopBar(
    topicName: String,
    isDetailOpen: Boolean,
    onCloseSection: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = topicName) },
        navigationIcon = {
            IconButton(onClick = if (isDetailOpen) onCloseSection else onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(
                        if (isDetailOpen) R.string.study_close_detail_description else R.string.study_back_description,
                    ),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}

@Composable
private fun StudySectionList(
    sections: List<StudySection>,
    onSelectSection: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = HorizontalPadding, vertical = SpaceMedium),
        verticalArrangement = Arrangement.spacedBy(SpaceSmall),
    ) {
        items(items = sections, key = { it.id }) { section ->
            StudySectionCard(section = section, onSelectSection = onSelectSection)
        }
    }
}

@Composable
private fun StudySectionCard(
    section: StudySection,
    onSelectSection: (String) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .testTag(STUDY_SECTION_CARD_TEST_TAG)
            .fillMaxWidth()
            .clickable(onClickLabel = stringResource(R.string.study_section_open_description)) {
                onSelectSection(section.id)
            }
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpaceMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = section.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(SpaceMedium))
            Column {
                Text(text = section.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = section.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun ErrorMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(HorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.study_error_generation),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}
