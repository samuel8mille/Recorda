package com.samuelribeiro.recorda

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkManager
import com.samuelribeiro.recorda.data.source.local.AppDatabase
import com.samuelribeiro.recorda.data.source.local.FlashcardReviewDao
import com.samuelribeiro.recorda.data.source.local.FlashcardReviewEntity
import com.samuelribeiro.recorda.data.source.local.ReviewLogDao
import com.samuelribeiro.recorda.data.source.local.ReviewLogEntity
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.local.TopicEntity
import com.samuelribeiro.recorda.data.source.local.TopicStatus
import com.samuelribeiro.recorda.presentation.ui.topic.composables.STATS_BUTTON_TEST_TAG
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
 * E2E flow for the retention statistics feature using seeded review logs and states,
 * so no network call is made. Exercises the full path: stats icon on the topic card →
 * navigation → StatsScreen registered by the :feature:stats dynamic module via
 * StatsSessionInitProvider → metric rows and weekly chart rendering.
 */
@HiltAndroidTest
class StatsFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var topicDao: TopicDao

    @Inject
    lateinit var reviewLogDao: ReviewLogDao

    @Inject
    lateinit var flashcardReviewDao: FlashcardReviewDao

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
    fun stats_icon_opens_screen_with_metrics() {
        seedTopicWithReviews()
        openStatsScreen()

        val totalLabel = composeRule.activity.getString(R.string.stats_total_cards)
        val successLabel = composeRule.activity.getString(R.string.stats_success_rate)
        composeRule.onNodeWithText(totalLabel).assertIsDisplayed()
        composeRule.onNodeWithText(successLabel).assertIsDisplayed()
        composeRule.onNodeWithText("2").assertIsDisplayed()
        composeRule.onNodeWithText("50%").assertIsDisplayed()
    }

    @Test
    fun stats_screen_shows_streak_and_chart_title() {
        seedTopicWithReviews()
        openStatsScreen()

        val chartTitle = composeRule.activity.getString(R.string.stats_chart_title)
        val streakValue = composeRule.activity.getString(R.string.stats_streak_value, 1)
        composeRule.onNodeWithText(chartTitle).assertIsDisplayed()
        composeRule.onNodeWithText(streakValue).assertIsDisplayed()
    }

    private fun seedTopicWithReviews() = runBlocking {
        topicDao.insert(
            TopicEntity(
                id = "e2e-stats-topic",
                name = topicName,
                flashcardsJson =
                """[{"question":"Q1?","answer":"A1"},{"question":"Q2?","answer":"A2"}]""",
                status = TopicStatus.DONE,
            )
        )
        flashcardReviewDao.upsert(
            FlashcardReviewEntity(
                id = "e2e-stats-topic_0",
                topicId = "e2e-stats-topic",
                cardIndex = 0,
                nextReviewAtMillis = System.currentTimeMillis() + 86_400_000L,
            )
        )
        val now = System.currentTimeMillis()
        reviewLogDao.insert(
            ReviewLogEntity(topicId = "e2e-stats-topic", cardIndex = 0, rating = "GOOD", timestampMillis = now)
        )
        reviewLogDao.insert(
            ReviewLogEntity(topicId = "e2e-stats-topic", cardIndex = 0, rating = "AGAIN", timestampMillis = now)
        )
    }

    private fun openStatsScreen() {
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithTag(STATS_BUTTON_TEST_TAG)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(STATS_BUTTON_TEST_TAG).performClick()
        val totalLabel = composeRule.activity.getString(R.string.stats_total_cards)
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithText(totalLabel)
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
