package com.samuelribeiro.recorda

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.samuelribeiro.recorda.presentation.ui.topic.composables.TOPIC_INPUT_TEST_TAG
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MainScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun appLaunches_topicInputIsVisible() {
        composeRule.onNodeWithTag(TOPIC_INPUT_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun appLaunches_emptyStateMessageIsVisible() {
        val expected = composeRule.activity.getString(R.string.topic_list_empty_message)
        composeRule.onNodeWithText(expected).assertIsDisplayed()
    }
}
