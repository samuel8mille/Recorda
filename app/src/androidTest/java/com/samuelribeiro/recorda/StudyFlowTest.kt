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
import com.samuelribeiro.recorda.data.source.local.TopicStatus
import com.samuelribeiro.recorda.presentation.ui.topic.composables.TOPIC_STUDY_AREA_TEST_TAG
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
 * E2E flow for the study guide feature using a pre-cached guide with null image URLs,
 * so no Gemini or Wikipedia call is made. Exercises the full path: tapping the topic
 * card name → navigation → StudyScreen registered by the :feature:study dynamic module
 * via StudySessionInitProvider → section list → detail view → back.
 */
@HiltAndroidTest
class StudyFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var topicDao: TopicDao

    private val topicName = "Programação Kotlin"
    private val sectionTitle = "Sintaxe básica"
    private val definition = "Sintaxe é o conjunto de regras da linguagem"
    private val content = "A sintaxe do Kotlin é concisa e expressiva"
    private val keyPoint = "Val declara valores imutáveis"
    private val analogy = "Como uma receita de bolo"

    private val studyGuideJson =
        """
        {"sections":[
          {"id":"0","title":"$sectionTitle","emoji":"📝","definition":"$definition","content":"$content",
           "summary":"Resumo da sintaxe.","keyPoints":["$keyPoint"],"analogy":"$analogy","imageUrl":null},
          {"id":"1","title":"Coroutines","emoji":"🧵","definition":"Concorrência estruturada","content":"Conteúdo",
           "summary":"Concorrência leve.","keyPoints":["Suspend functions"],"imageUrl":null}
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
    fun tapping_topic_name_opens_study_guide_with_cached_sections() {
        seedTopicWithStudyGuide()
        openStudyScreen()

        composeRule.onNodeWithText(sectionTitle).assertIsDisplayed()
        composeRule.onNodeWithText("Coroutines").assertIsDisplayed()
    }

    @Test
    fun tapping_section_card_opens_detail_with_learning_aids() {
        seedTopicWithStudyGuide()
        openStudyScreen()

        composeRule.onNodeWithText(sectionTitle).performClick()

        composeRule.onNodeWithText(definition).assertIsDisplayed()
        composeRule.onNodeWithText(content).assertIsDisplayed()
        composeRule.onNodeWithText(keyPoint, substring = true).assertIsDisplayed()
        composeRule.onNodeWithText(analogy).assertIsDisplayed()
    }

    @Test
    fun back_from_detail_returns_to_section_list() {
        seedTopicWithStudyGuide()
        openStudyScreen()
        composeRule.onNodeWithText(sectionTitle).performClick()
        composeRule.onNodeWithText(analogy).assertIsDisplayed()

        Espresso.pressBack()

        composeRule.onNodeWithText("Coroutines").assertIsDisplayed()
        composeRule.onNodeWithText(analogy).assertDoesNotExist()
    }

    private fun seedTopicWithStudyGuide() = runBlocking {
        topicDao.insert(
            TopicEntity(
                id = "e2e-study-topic",
                name = topicName,
                flashcardsJson = """[{"question":"O que é Kotlin?","answer":"Linguagem JVM moderna"}]""",
                status = TopicStatus.DONE,
                studyGuideJson = studyGuideJson,
            )
        )
    }

    private fun openStudyScreen() {
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithTag(TOPIC_STUDY_AREA_TEST_TAG)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag(TOPIC_STUDY_AREA_TEST_TAG).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithText(sectionTitle)
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
