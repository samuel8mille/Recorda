package com.samuelribeiro.recorda.data.repository

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.gson.Gson
import com.samuelribeiro.recorda.core.network.NetworkError
import com.samuelribeiro.recorda.core.network.ServiceExecutor
import com.samuelribeiro.recorda.data.mapper.FlashcardMapper
import com.samuelribeiro.recorda.data.mapper.TopicEntityMapper
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.local.TopicEntity
import com.samuelribeiro.recorda.data.source.local.TopicStatus
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.prompt.FlashcardPromptBuilder
import com.samuelribeiro.recorda.logging.CrashReporter
import com.samuelribeiro.recorda.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
    private val workManager: WorkManager = mockk(relaxed = true)
    private val crashReporter: CrashReporter = mockk(relaxed = true)
    private val promptBuilder: FlashcardPromptBuilder = mockk {
        every { build(any()) } answers { "prompt for ${firstArg<String>()}" }
    }

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
            workManager = workManager,
            crashlyticsReporter = crashReporter,
            promptBuilder = promptBuilder,
        )
    }

    @Test
    fun `getStoredTopics maps DAO entities to domain topics`() = runTest {
        val flashcardsJson = Gson().toJson(listOf(Flashcard("Q?", "A")))
        val entity = TopicEntity("1", "Kotlin", flashcardsJson, TopicStatus.DONE)
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
    fun `generateFlashcards success returns topic and inserts to DB`() = runTest {
        coEvery { geminiService.generateContent(any()) } returns "P: What is Kotlin? | R: A JVM language"

        val results = repository.generateFlashcards("Kotlin").toList()

        assertEquals(1, results.size)
        val topic = results.first().getOrThrow()
        assertEquals("Kotlin", topic.name)
        assertEquals(1, topic.flashcards.size)
        assertEquals("What is Kotlin?", topic.flashcards[0].question)
        coVerify(exactly = 1) { topicDao.insert(match<TopicEntity> { it.status == TopicStatus.DONE }) }
    }

    @Test
    fun `generateFlashcards NoInternet inserts PENDING entity and enqueues WorkManager`() = runTest {
        coEvery { geminiService.generateContent(any()) } throws NetworkError.NoInternet()

        val results = repository.generateFlashcards("Kotlin").toList()

        assertTrue(results.first().isFailure)
        assertIs<NetworkError.NoInternet>(results.first().exceptionOrNull())
        coVerify { topicDao.insert(match<TopicEntity> { it.status == TopicStatus.PENDING && it.name == "Kotlin" }) }
        verify { workManager.enqueueUniqueWork(any<String>(), any<ExistingWorkPolicy>(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `generateFlashcards Timeout inserts PENDING entity and enqueues WorkManager`() = runTest {
        coEvery { geminiService.generateContent(any()) } throws NetworkError.Timeout()

        val results = repository.generateFlashcards("Kotlin").toList()

        assertTrue(results.first().isFailure)
        coVerify { topicDao.insert(match<TopicEntity> { it.status == TopicStatus.PENDING }) }
        verify { workManager.enqueueUniqueWork(any<String>(), any<ExistingWorkPolicy>(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `generateFlashcards HttpError does not enqueue WorkManager`() = runTest {
        coEvery { geminiService.generateContent(any()) } throws NetworkError.HttpError(500, "Server Error")

        val results = repository.generateFlashcards("Kotlin").toList()

        assertTrue(results.first().isFailure)
        verify(exactly = 0) { workManager.enqueueUniqueWork(any<String>(), any<ExistingWorkPolicy>(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `getTopic maps entity to domain topic`() = runTest {
        val flashcardsJson = Gson().toJson(listOf(Flashcard("Q?", "A")))
        val entity = TopicEntity("1", "Kotlin", flashcardsJson, TopicStatus.DONE)
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

    @Test
    fun `generateFlashcards includes topic name in DB insert on success`() = runTest {
        coEvery { geminiService.generateContent(any()) } returns "P: Q? | R: A"

        repository.generateFlashcards("História do Brasil").toList()

        coVerify { topicDao.insert(match<TopicEntity> { it.name == "História do Brasil" }) }
    }
}
