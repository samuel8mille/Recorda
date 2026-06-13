package com.samuelribeiro.recorda

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkManager
import com.samuelribeiro.recorda.data.source.local.AppDatabase
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.local.TopicEntity
import com.samuelribeiro.recorda.presentation.ui.topic.composables.DELETE_BUTTON_TEST_TAG
import com.samuelribeiro.recorda.presentation.ui.topic.composables.TOPIC_ITEM_TEST_TAG
import com.samuelribeiro.recorda.presentation.ui.topichub.composables.HUB_CONTENT_TEST_TAG
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class TopicFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var topicDao: TopicDao

    private val seededTopicId = "e2e-topic-1"
    private val seededTopicName = "Programação Kotlin"
    private val seededFlashcardsJson =
        """[{"question":"O que é Kotlin?","answer":"Linguagem JVM moderna"}]"""

    @Before
    fun setUp() {
        hiltRule.inject()
        database.clearAllTables()
    }

    @After
    fun tearDown() {
        database.clearAllTables()
    }

    @Test
    fun seeded_topic_shows_as_card_with_name() {
        seedTopic()

        waitForTopicCard()

        composeRule.onNodeWithText(seededTopicName).assertIsDisplayed()
    }

    @Test
    fun tapping_topic_card_opens_the_hub() {
        seedTopic()
        waitForTopicCard()

        composeRule.onNodeWithTag(TOPIC_ITEM_TEST_TAG).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithTag(HUB_CONTENT_TEST_TAG)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(HUB_CONTENT_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun delete_button_shows_confirmation_dialog() {
        seedTopic()
        waitForTopicCard()

        composeRule.onNodeWithTag(DELETE_BUTTON_TEST_TAG).performClick()

        val dialogTitle = composeRule.activity.getString(R.string.topic_delete_dialog_title)
        composeRule.onNodeWithText(dialogTitle).assertIsDisplayed()
    }

    @Test
    fun cancel_delete_dialog_dismisses_it() {
        seedTopic()
        waitForTopicCard()
        composeRule.onNodeWithTag(DELETE_BUTTON_TEST_TAG).performClick()
        val dialogTitle = composeRule.activity.getString(R.string.topic_delete_dialog_title)
        composeRule.onNodeWithText(dialogTitle).assertIsDisplayed()

        val cancelLabel = composeRule.activity.getString(R.string.topic_delete_dialog_cancel)
        composeRule.onNodeWithText(cancelLabel).performClick()

        composeRule.onNodeWithText(dialogTitle).assertDoesNotExist()
    }

    private fun seedTopic() = runBlocking {
        topicDao.insert(
            TopicEntity(
                id = seededTopicId,
                name = seededTopicName,
                flashcardsJson = seededFlashcardsJson,
            )
        )
    }

    private fun waitForTopicCard() {
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithTag(DELETE_BUTTON_TEST_TAG)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpWorkManager() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            runCatching { WorkManager.initialize(context, Configuration.Builder().build()) }
        }
    }
}
