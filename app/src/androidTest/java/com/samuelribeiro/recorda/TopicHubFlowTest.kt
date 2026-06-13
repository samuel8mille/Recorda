package com.samuelribeiro.recorda

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkManager
import com.samuelribeiro.recorda.data.source.local.AppDatabase
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.local.TopicEntity
import com.samuelribeiro.recorda.presentation.ui.topic.composables.TOPIC_ITEM_TEST_TAG
import com.samuelribeiro.recorda.presentation.ui.topichub.composables.HUB_CONTENT_TEST_TAG
import com.samuelribeiro.recorda.presentation.ui.topichub.composables.HUB_MIND_MAP_TEST_TAG
import com.samuelribeiro.recorda.presentation.ui.topichub.composables.HUB_REVIEW_TEST_TAG
import com.samuelribeiro.recorda.presentation.ui.topichub.composables.HUB_STATS_TEST_TAG
import com.samuelribeiro.recorda.presentation.ui.topichub.composables.HUB_STUDY_TEST_TAG
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * E2E flow for the topic hub: tapping a topic card opens a grid of the five learning
 * materials, and the back button returns to the home list.
 */
@HiltAndroidTest
class TopicHubFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var topicDao: TopicDao

    private val topicName = "Programação Kotlin"

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
    fun hub_shows_all_five_learning_materials() {
        seedTopic()
        openHub()

        composeRule.onNodeWithTag(HUB_CONTENT_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(HUB_STUDY_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(HUB_MIND_MAP_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(HUB_REVIEW_TEST_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(HUB_STATS_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun back_from_hub_returns_to_home() {
        seedTopic()
        openHub()

        Espresso.pressBack()

        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithTag(TOPIC_ITEM_TEST_TAG)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText(topicName).assertIsDisplayed()
    }

    private fun seedTopic() = runBlocking {
        topicDao.insert(
            TopicEntity(
                id = "e2e-hub-topic",
                name = topicName,
                flashcardsJson = """[{"question":"Q?","answer":"A"}]""",
            )
        )
    }

    private fun openHub() {
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithTag(TOPIC_ITEM_TEST_TAG)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(TOPIC_ITEM_TEST_TAG).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithTag(HUB_CONTENT_TEST_TAG)
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
