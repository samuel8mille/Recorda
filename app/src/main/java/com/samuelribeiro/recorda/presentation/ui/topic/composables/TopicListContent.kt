package com.samuelribeiro.recorda.presentation.ui.topic.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.samuelribeiro.recorda.R
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.ui.theme.HorizontalPadding
import com.samuelribeiro.recorda.ui.theme.SpaceLarge
import com.samuelribeiro.recorda.ui.theme.SpaceMedium
import com.samuelribeiro.recorda.ui.theme.SpaceSmall
import com.samuelribeiro.recorda.presentation.ui.topic.TopicUiState

const val ADD_TOPIC_BUTTON_TEST_TAG = "AddTopicButtonTestTag"
const val INPUT_ERROR_TEST_TAG = "InputErrorTestTag"
const val TOPIC_ITEM_TEST_TAG = "TopicItemTestTag"
const val TOPIC_INPUT_TEST_TAG = "TopicInput"
const val DELETE_BUTTON_TEST_TAG = "DeleteButtonTestTag"

@Composable
fun TopicListContent(
    uiState: TopicUiState,
    onAddTopicClick: (String) -> Unit,
    onTopicClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
) {
    val (topic, setTopic) = remember { mutableStateOf("") }

    if (uiState.pendingDeleteTopicId != null) {
        val topicName = uiState.topics.find { it.id == uiState.pendingDeleteTopicId }?.name ?: ""
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(stringResource(R.string.topic_delete_dialog_title)) },
            text = { Text(stringResource(R.string.topic_delete_dialog_message, topicName)) },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(
                        text = stringResource(R.string.topic_delete_dialog_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text(stringResource(R.string.topic_delete_dialog_cancel))
                }
            },
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(SpaceLarge))
        TopicContentHeader(
            uiState = uiState,
            onAddTopicClick = { value ->
                onAddTopicClick(value)
                setTopic("")
            },
            topicValue = topic,
            onTopicChange = setTopic,
        )
        if (uiState.topics.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                EmptyTopicListMessage()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = HorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(SpaceSmall),
                contentPadding = PaddingValues(vertical = SpaceMedium),
            ) {
                items(items = uiState.topics) { item ->
                    TopicContentListItem(
                        item = item,
                        onTopicClick = onTopicClick,
                        onDeleteClick = onDeleteClick,
                    )
                }
            }
        }
    }
}

@Composable
fun TopicContentHeader(
    uiState: TopicUiState,
    topicValue: String,
    onTopicChange: (String) -> Unit,
    onAddTopicClick: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val errorMessage = uiState.inputError?.let { stringResource(it.messageRes) }

    Surface {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column {
                    OutlinedTextField(
                        modifier = Modifier
                            .testTag(TOPIC_INPUT_TEST_TAG)
                            .focusRequester(focusRequester)
                            .then(
                                if (errorMessage != null) Modifier.semantics { error(errorMessage) }
                                else Modifier
                            ),
                        value = topicValue,
                        onValueChange = onTopicChange,
                        placeholder = { Text(text = stringResource(R.string.topic_input_hint)) },
                        isError = uiState.inputError != null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                onAddTopicClick(topicValue.trim())
                                onTopicChange("")
                                focusRequester.requestFocus()
                            }
                        )
                    )
                    uiState.inputError?.let {
                        Text(
                            modifier = Modifier
                                .testTag(INPUT_ERROR_TEST_TAG)
                                .semantics { liveRegion = LiveRegionMode.Polite },
                            text = stringResource(it.messageRes),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(Modifier.width(SpaceMedium))
                Button(
                    modifier = Modifier.testTag(ADD_TOPIC_BUTTON_TEST_TAG),
                    onClick = {
                        onAddTopicClick(topicValue.trim())
                        onTopicChange("")
                        focusRequester.requestFocus()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.topic_button_add_description)
                    )
                }
            }
            Spacer(Modifier.height(SpaceLarge))
            Text(
                modifier = Modifier.semantics { heading() },
                text = stringResource(R.string.topic_list_title),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(SpaceMedium))
        }
    }
}

@Composable
fun TopicContentListItem(
    item: Topic,
    onTopicClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .testTag(TOPIC_ITEM_TEST_TAG)
            .fillMaxWidth()
            .clickable(onClickLabel = stringResource(R.string.topic_open_description)) {
                onTopicClick(item.id)
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = HorizontalPadding, end = SpaceSmall, top = SpaceMedium, bottom = SpaceMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .wrapContentHeight(),
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(
                modifier = Modifier.testTag(DELETE_BUTTON_TEST_TAG),
                onClick = { onDeleteClick(item.id) },
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.topic_delete_button_description),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
fun EmptyTopicListMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = SpaceLarge * 2),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(SpaceMedium))
        Text(
            text = stringResource(R.string.topic_list_empty_message),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
    }
}
