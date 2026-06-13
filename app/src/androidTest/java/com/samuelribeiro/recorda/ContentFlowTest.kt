package com.samuelribeiro.recorda

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
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
 * E2E flow for the content feature using a pre-cached, complete chapter content, so no
 * Gemini call is made. Exercises the full path: topic card → hub → ContentScreen
 * registered by the :feature:content dynamic module via ContentSessionInitProvider →
 * chapter list → chapter detail → back.
 */
@HiltAndroidTest
class ContentFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var topicDao: TopicDao

    private val topicName = "Programação Kotlin"
    private val chapterTitle = "Introdução"
    private val chapterBody = "Kotlin é uma linguagem de programação moderna para a JVM."

    private val contentJson =
        """
        {"chapters":[
          {"id":"0","title":"$chapterTitle","summary":"Visão geral","body":"$chapterBody"},
          {"id":"1","title":"Funções","summary":"Como declarar funções","body":"Funções usam a palavra-chave fun."}
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
    fun content_shows_chapter_list_from_cache() {
        seedTopicWithContent()
        openContentScreen()

        composeRule.onNodeWithText(chapterTitle).assertIsDisplayed()
        composeRule.onNodeWithText("Funções").assertIsDisplayed()
    }

    @Test
    fun tapping_chapter_opens_detail_with_body() {
        seedTopicWithContent()
        openContentScreen()

        composeRule.onNodeWithText(chapterTitle).performClick()

        composeRule.onNodeWithText(chapterBody).assertIsDisplayed()
    }

    @Test
    fun back_from_detail_returns_to_chapter_list() {
        seedTopicWithContent()
        openContentScreen()
        composeRule.onNodeWithText(chapterTitle).performClick()
        composeRule.onNodeWithText(chapterBody).assertIsDisplayed()

        Espresso.pressBack()

        composeRule.onNodeWithText("Funções").assertIsDisplayed()
        composeRule.onNodeWithText(chapterBody).assertDoesNotExist()
    }

    private fun seedTopicWithContent() = runBlocking {
        topicDao.insert(
            TopicEntity(
                id = "e2e-content-topic",
                name = topicName,
                flashcardsJson = """[{"question":"Q?","answer":"A"}]""",
                contentJson = contentJson,
            )
        )
    }

    private fun openContentScreen() {
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
        composeRule.onNodeWithTag(HUB_CONTENT_TEST_TAG).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithText(chapterTitle)
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
