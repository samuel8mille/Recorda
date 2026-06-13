package com.samuelribeiro.recorda

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import android.Manifest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkManager
import com.samuelribeiro.recorda.data.source.local.AppDatabase
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.local.TopicEntity
import com.samuelribeiro.recorda.presentation.ui.topic.composables.TOPIC_ITEM_TEST_TAG
import com.samuelribeiro.recorda.presentation.ui.topichub.composables.HUB_ACTIVE_RECALL_TEST_TAG
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
 * E2E flow for the active-recall session using a pre-cached deck, so no Gemini call is made.
 * Exercises the path: topic card → hub → ActiveRecallScreen registered by the
 * :feature:active_recall dynamic module → first card shown with its definition. The flow stops
 * before the auto-recording phase, which depends on the device microphone and is non-deterministic.
 */
@HiltAndroidTest
class ActiveRecallFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var topicDao: TopicDao

    private val topicName = "Biologia"
    private val concept = "Fotossíntese"
    private val definition = "Processo que converte luz solar em energia química nas plantas."

    private val memoryCardsJson =
        """
        {"cards":[
          {"id":"0","concept":"$concept","definition":"$definition"},
          {"id":"1","concept":"Mitose","definition":"Divisão celular que gera células idênticas."}
        ]}
        """.trimIndent()

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
    fun active_recall_shows_first_card_with_definition_from_cache() {
        seedTopicWithDeck()
        openActiveRecallScreen()

        composeRule.onNodeWithText(concept).assertIsDisplayed()
        composeRule.onNodeWithText(definition).assertIsDisplayed()
    }

    private fun seedTopicWithDeck() = runBlocking {
        topicDao.insert(
            TopicEntity(
                id = "e2e-recall-topic",
                name = topicName,
                flashcardsJson = """[{"question":"Q?","answer":"A"}]""",
                memoryCardsJson = memoryCardsJson,
            )
        )
    }

    private fun openActiveRecallScreen() {
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithText(topicName)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(TOPIC_ITEM_TEST_TAG).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithTag(HUB_ACTIVE_RECALL_TEST_TAG).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(HUB_ACTIVE_RECALL_TEST_TAG).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithText(concept).fetchSemanticsNodes().isNotEmpty()
        }
    }

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpEnvironment() {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            runCatching { WorkManager.initialize(instrumentation.targetContext, Configuration.Builder().build()) }
            runCatching {
                instrumentation.uiAutomation.grantRuntimePermission(
                    instrumentation.targetContext.packageName,
                    Manifest.permission.RECORD_AUDIO,
                )
            }
        }
    }
}
