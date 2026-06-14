package com.samuelribeiro.recorda.data.repository

import com.google.gson.Gson
import com.samuelribeiro.recorda.core.network.NetworkError
import com.samuelribeiro.recorda.core.network.ServiceExecutor
import com.samuelribeiro.recorda.data.mapper.FlashcardMapper
import com.samuelribeiro.recorda.data.mapper.TopicEntityMapper
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.local.TopicEntity
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.data.sync.SyncCommandDispatcher
import com.samuelribeiro.recorda.data.sync.SyncCommandType
import com.samuelribeiro.recorda.data.sync.UpsertTopicFlashcardsPayload
import com.samuelribeiro.recorda.domain.model.Chapter
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.model.TopicContent
import com.samuelribeiro.recorda.domain.prompt.FlashcardPromptBuilder
import com.samuelribeiro.recorda.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TopicRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val geminiService: GeminiService = mockk()
    private val flashcardMapper = FlashcardMapper()
    private val topicEntityMapper = TopicEntityMapper(Gson())
    private val topicDao: TopicDao = mockk(relaxed = true)
    private val promptBuilder: FlashcardPromptBuilder = mockk {
        every { build(any(), any()) } answers { "prompt for ${firstArg<String>()}" }
    }
    private val syncCommandDispatcher: SyncCommandDispatcher = mockk(relaxed = true)

    private val serviceExecutor by lazy {
        ServiceExecutor(ioDispatcher = mainDispatcherRule.testDispatcher)
    }

    private val repository by lazy {
        TopicRepositoryImpl(
            geminiService = geminiService,
            flashcardMapper = flashcardMapper,
            topicEntityMapper = topicEntityMapper,
            serviceExecutor = serviceExecutor,
            topicDao = topicDao,
            gson = Gson(),
            promptBuilder = promptBuilder,
            syncCommandDispatcher = syncCommandDispatcher,
        )
    }

    private fun topicWithContent() = Topic(
        id = "1",
        name = "Kotlin",
        flashcards = emptyList(),
        content = TopicContent(listOf(Chapter("0", "Intro", "Resumo", "Corpo do capítulo"))),
    )

    @Test
    fun `getStoredTopics maps DAO entities to domain topics`() = runTest {
        val flashcardsJson = Gson().toJson(listOf(Flashcard("Q?", "A")))
        val entity = TopicEntity("1", "Kotlin", flashcardsJson)
        every { topicDao.getAll() } returns flowOf(listOf(entity))

        val topics = repository.getStoredTopics().first()

        assertEquals(1, topics.size)
        assertEquals("Kotlin", topics[0].name)
        assertEquals("Q?", topics[0].flashcards[0].question)
    }

    @Test
    fun `getStoredTopics returns empty list when DAO is empty`() = runTest {
        every { topicDao.getAll() } returns flowOf(emptyList())

        val topics = repository.getStoredTopics().first()

        assertTrue(topics.isEmpty())
    }

    @Test
    fun `createTopic inserts an empty topic without calling the network`() = runTest {
        val topic = repository.createTopic("História do Brasil")

        assertEquals("História do Brasil", topic.name)
        assertTrue(topic.flashcards.isEmpty())
        coVerify { topicDao.insert(match<TopicEntity> { it.name == "História do Brasil" }) }
        coVerify(exactly = 0) { geminiService.generateContent(any()) }
    }

    @Test
    fun `createTopic enqueues a CREATE_TOPIC sync command`() = runTest {
        val topic = repository.createTopic("História do Brasil")

        coVerify {
            syncCommandDispatcher.enqueue(SyncCommandType.CREATE_TOPIC, topic.id, any())
        }
    }

    @Test
    fun `deleteTopic enqueues a DELETE_TOPIC sync command`() = runTest {
        repository.deleteTopic("1")

        coVerify { topicDao.deleteById("1") }
        coVerify { syncCommandDispatcher.enqueue(SyncCommandType.DELETE_TOPIC, "1", any()) }
    }

    @Test
    fun `generateFlashcards success returns cards and updates DB`() = runTest {
        coEvery { geminiService.generateContent(any()) } returns "P: What is Kotlin? | R: A JVM language"

        val results = repository.generateFlashcards(topicWithContent()).toList()

        assertEquals(1, results.size)
        val flashcards = results.first().getOrThrow()
        assertEquals(1, flashcards.size)
        assertEquals("What is Kotlin?", flashcards[0].question)
        coVerify(exactly = 1) { topicDao.updateFlashcards("1", any(), any()) }
    }

    @Test
    fun `generateFlashcards success enqueues an UPSERT_TOPIC_FLASHCARDS sync command`() = runTest {
        coEvery { geminiService.generateContent(any()) } returns "P: What is Kotlin? | R: A JVM language"

        repository.generateFlashcards(topicWithContent()).toList()

        coVerify {
            syncCommandDispatcher.enqueue(
                SyncCommandType.UPSERT_TOPIC_FLASHCARDS,
                "1",
                match<UpsertTopicFlashcardsPayload> { it.topicId == "1" },
            )
        }
    }

    @Test
    fun `generateFlashcards failure propagates and does not update DB`() = runTest {
        coEvery { geminiService.generateContent(any()) } throws NetworkError.NoInternet()

        val results = repository.generateFlashcards(topicWithContent()).toList()

        assertTrue(results.first().isFailure)
        assertIs<NetworkError.NoInternet>(results.first().exceptionOrNull())
        coVerify(exactly = 0) { topicDao.updateFlashcards(any(), any(), any()) }
        coVerify(exactly = 0) {
            syncCommandDispatcher.enqueue(SyncCommandType.UPSERT_TOPIC_FLASHCARDS, any(), any())
        }
    }

    @Test
    fun `getTopic maps entity to domain topic`() = runTest {
        val flashcardsJson = Gson().toJson(listOf(Flashcard("Q?", "A")))
        val entity = TopicEntity("1", "Kotlin", flashcardsJson)
        every { topicDao.getById("1") } returns flowOf(entity)

        val topic = repository.getTopic("1").first()

        assertEquals("1", topic?.id)
        assertEquals("Kotlin", topic?.name)
        assertEquals("Q?", topic?.flashcards?.first()?.question)
    }

    @Test
    fun `getTopic returns null when entity not found`() = runTest {
        every { topicDao.getById("missing") } returns flowOf(null)

        val topic = repository.getTopic("missing").first()

        kotlin.test.assertNull(topic)
    }
}
