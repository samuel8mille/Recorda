package com.samuelribeiro.recorda

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkManager
import com.samuelribeiro.recorda.data.source.local.AppDatabase
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.local.TopicEntity
import com.samuelribeiro.recorda.presentation.ui.topic.composables.TOPIC_ITEM_TEST_TAG
import com.samuelribeiro.recorda.presentation.ui.topichub.composables.HUB_MIND_MAP_TEST_TAG
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
 * E2E flow for the mind map feature using a pre-cached mind map, so no Gemini
 * call is made. Exercises the full path: topic card button → navigation →
 * MindMapScreen registered by the :feature:mind_map dynamic module via
 * MindMapSessionInitProvider → tree rendering and expand/collapse.
 */
@HiltAndroidTest
class MindMapFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var topicDao: TopicDao

    private val topicName = "Programação Kotlin"
    private val childTitle = "Sintaxe"
    private val grandchildTitle = "Funções de extensão"

    private val mindMapJson =
        """
        {"id":"0","title":"$topicName","children":[
          {"id":"0-0","title":"$childTitle","children":[
            {"id":"0-0-0","title":"$grandchildTitle","children":[]}
          ]}
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
    fun mind_map_button_opens_screen_with_cached_tree() {
        seedTopicWithMindMap()
        openMindMapScreen()

        composeRule.onAllNodesWithText(topicName)[0].assertIsDisplayed()
        composeRule.onNodeWithText(childTitle).assertIsDisplayed()
    }

    @Test
    fun expanding_node_reveals_its_children() {
        seedTopicWithMindMap()
        openMindMapScreen()
        composeRule.onNodeWithText(grandchildTitle).assertDoesNotExist()

        val expandLabel = composeRule.activity.getString(R.string.mind_map_expand_description)
        composeRule.onNodeWithContentDescription(expandLabel).performClick()

        composeRule.onNodeWithText(grandchildTitle).assertIsDisplayed()
    }

    @Test
    fun collapsing_expanded_node_hides_its_children() {
        seedTopicWithMindMap()
        openMindMapScreen()
        val expandLabel = composeRule.activity.getString(R.string.mind_map_expand_description)
        composeRule.onNodeWithContentDescription(expandLabel).performClick()
        composeRule.onNodeWithText(grandchildTitle).assertIsDisplayed()

        val collapseLabel = composeRule.activity.getString(R.string.mind_map_collapse_description)
        composeRule.onAllNodesWithContentDescription(collapseLabel)[1].performClick()

        composeRule.onNodeWithText(grandchildTitle).assertDoesNotExist()
    }

    private fun seedTopicWithMindMap() = runBlocking {
        topicDao.insert(
            TopicEntity(
                id = "e2e-mindmap-topic",
                name = topicName,
                flashcardsJson = """[{"question":"O que é Kotlin?","answer":"Linguagem JVM moderna"}]""",
                mindMapJson = mindMapJson,
            )
        )
    }

    private fun openMindMapScreen() {
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithTag(TOPIC_ITEM_TEST_TAG)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(TOPIC_ITEM_TEST_TAG).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithTag(HUB_MIND_MAP_TEST_TAG)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(HUB_MIND_MAP_TEST_TAG).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithText(childTitle)
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
