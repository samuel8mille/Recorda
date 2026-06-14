package com.samuelribeiro.recorda.data.repository

import com.google.gson.Gson
import com.samuelribeiro.recorda.core.network.NetworkError
import com.samuelribeiro.recorda.core.network.ServiceExecutor
import com.samuelribeiro.recorda.data.mapper.TopicContentMapper
import com.samuelribeiro.recorda.data.prompt.GeminiTopicContentPromptBuilder
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.data.sync.SyncCommandDispatcher
import com.samuelribeiro.recorda.data.sync.SyncCommandType
import com.samuelribeiro.recorda.domain.model.Chapter
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.model.TopicContent
import com.samuelribeiro.recorda.domain.model.TopicContentStep
import com.samuelribeiro.recorda.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TopicContentRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val geminiService: GeminiService = mockk()
    private val topicDao: TopicDao = mockk(relaxed = true)
    private val mapper = TopicContentMapper()
    private val promptBuilder = GeminiTopicContentPromptBuilder()
    private val syncCommandDispatcher: SyncCommandDispatcher = mockk(relaxed = true)
    private val serviceExecutor by lazy { ServiceExecutor(ioDispatcher = mainDispatcherRule.testDispatcher) }

    private val repository by lazy {
        TopicContentRepositoryImpl(
            geminiService = geminiService,
            topicContentMapper = mapper,
            promptBuilder = promptBuilder,
            serviceExecutor = serviceExecutor,
            topicDao = topicDao,
            gson = Gson(),
            syncCommandDispatcher = syncCommandDispatcher,
        )
    }

    private fun stubResponses(listResponse: String) {
        coEvery { geminiService.generateContent(any()) } answers {
            val prompt = firstArg<String>()
            when {
                prompt.contains("Monte a estrutura") -> listResponse
                prompt.contains("Causas") -> "Corpo das causas"
                prompt.contains("Consequências") -> "Corpo das consequências"
                else -> "Corpo genérico"
            }
        }
    }

    @Test
    fun `generates chapter list then each body in order`() = runTest {
        stubResponses("T: Causas | S: As origens\nT: Consequências | S: Os efeitos")
        val topic = Topic("1", "Guerra", emptyList())

        val steps = repository.generateTopicContent(topic).toList().map { it.getOrThrow() }

        assertIs<TopicContentStep.ChaptersPlanned>(steps[0])
        assertIs<TopicContentStep.ChapterGenerated>(steps[1])
        assertEquals(0, (steps[1] as TopicContentStep.ChapterGenerated).chapterIndex)
        assertIs<TopicContentStep.ChapterGenerated>(steps[2])
        assertEquals(1, (steps[2] as TopicContentStep.ChapterGenerated).chapterIndex)
        val completed = steps[3]
        assertIs<TopicContentStep.Completed>(completed)
        assertTrue(completed.content.isComplete)
        assertEquals("Corpo das causas", completed.content.chapters[0].body)
        assertEquals("Corpo das consequências", completed.content.chapters[1].body)
    }

    @Test
    fun `persists content incrementally after list and each body`() = runTest {
        stubResponses("T: Causas | S: As origens\nT: Consequências | S: Os efeitos")
        val topic = Topic("1", "Guerra", emptyList())

        repository.generateTopicContent(topic).toList()

        coVerify(exactly = 3) { topicDao.updateContent("1", any(), any()) }
    }

    @Test
    fun `persists content incrementally enqueues an UPSERT_TOPIC_CONTENT sync command per step`() = runTest {
        stubResponses("T: Causas | S: As origens\nT: Consequências | S: Os efeitos")
        val topic = Topic("1", "Guerra", emptyList())

        repository.generateTopicContent(topic).toList()

        coVerify(exactly = 3) {
            syncCommandDispatcher.enqueue(SyncCommandType.UPSERT_TOPIC_CONTENT, "1", any())
        }
    }

    @Test
    fun `resumes only missing bodies when content is partial`() = runTest {
        stubResponses("ignored")
        val partial = TopicContent(
            listOf(
                Chapter("0", "Causas", "As origens", body = "Corpo das causas"),
                Chapter("1", "Consequências", "Os efeitos", body = ""),
            ),
        )
        val topic = Topic("1", "Guerra", emptyList(), content = partial)

        val steps = repository.generateTopicContent(topic).toList().map { it.getOrThrow() }

        assertEquals(2, steps.size)
        assertIs<TopicContentStep.ChapterGenerated>(steps[0])
        assertEquals(1, (steps[0] as TopicContentStep.ChapterGenerated).chapterIndex)
        assertIs<TopicContentStep.Completed>(steps[1])
        coVerify(exactly = 1) { geminiService.generateContent(any()) }
        coVerify(exactly = 1) { topicDao.updateContent("1", any(), any()) }
    }

    @Test
    fun `propagates failure on chapter list and keeps nothing persisted`() = runTest {
        coEvery { geminiService.generateContent(any()) } throws NetworkError.NoInternet()
        val topic = Topic("1", "Guerra", emptyList())

        val results = repository.generateTopicContent(topic).toList()

        assertTrue(results.last().isFailure)
        coVerify(exactly = 0) { topicDao.updateContent(any(), any(), any()) }
        coVerify(exactly = 0) {
            syncCommandDispatcher.enqueue(SyncCommandType.UPSERT_TOPIC_CONTENT, any(), any())
        }
    }

    @Test
    fun `propagates failure on a body and keeps partial progress cached`() = runTest {
        coEvery { geminiService.generateContent(any()) } answers {
            val prompt = firstArg<String>()
            if (prompt.contains("Monte a estrutura")) {
                "T: Causas | S: As origens\nT: Consequências | S: Os efeitos"
            } else {
                throw NetworkError.Timeout()
            }
        }
        val topic = Topic("1", "Guerra", emptyList())

        val results = repository.generateTopicContent(topic).toList()

        assertIs<TopicContentStep.ChaptersPlanned>(results.first().getOrThrow())
        assertTrue(results.last().isFailure)
        coVerify(exactly = 1) { topicDao.updateContent("1", any(), any()) }
    }
}
