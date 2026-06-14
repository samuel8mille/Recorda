package com.samuelribeiro.recorda.data.repository

import com.google.gson.Gson
import com.samuelribeiro.recorda.core.network.NetworkError
import com.samuelribeiro.recorda.core.network.ServiceExecutor
import com.samuelribeiro.recorda.data.mapper.MemoryDeckMapper
import com.samuelribeiro.recorda.data.prompt.GeminiMemoryDeckPromptBuilder
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.data.sync.SyncCommandDispatcher
import com.samuelribeiro.recorda.data.sync.SyncCommandType
import com.samuelribeiro.recorda.data.sync.UpsertTopicMemoryCardsPayload
import com.samuelribeiro.recorda.domain.model.Chapter
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.model.TopicContent
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
class MemoryDeckRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val geminiService: GeminiService = mockk()
    private val memoryDeckMapper = MemoryDeckMapper()
    private val topicDao: TopicDao = mockk(relaxed = true)
    private val gson = Gson()
    private val promptBuilder = GeminiMemoryDeckPromptBuilder()
    private val syncCommandDispatcher: SyncCommandDispatcher = mockk(relaxed = true)
    private val serviceExecutor by lazy { ServiceExecutor(ioDispatcher = mainDispatcherRule.testDispatcher) }

    private val repository by lazy {
        MemoryDeckRepositoryImpl(
            geminiService = geminiService,
            memoryDeckMapper = memoryDeckMapper,
            serviceExecutor = serviceExecutor,
            topicDao = topicDao,
            gson = gson,
            promptBuilder = promptBuilder,
            syncCommandDispatcher = syncCommandDispatcher,
        )
    }

    private val topic = Topic(
        id = "1",
        name = "Biologia",
        flashcards = emptyList(),
        content = TopicContent(listOf(Chapter("0", "Célula", "resumo", "corpo"))),
    )

    @Test
    fun `generateMemoryDeck success maps response and persists json`() = runTest {
        coEvery { geminiService.generateContent(any()) } returns
            "C: Célula | D: Unidade da vida\nC: DNA | D: Molécula da hereditariedade"

        val results = repository.generateMemoryDeck(topic).toList()

        val deck = results.first().getOrThrow()
        assertEquals(2, deck.cards.size)
        assertEquals("Célula", deck.cards[0].concept)
        coVerify { topicDao.updateMemoryCards("1", gson.toJson(deck), any()) }
    }

    @Test
    fun `generateMemoryDeck success enqueues an UPSERT_TOPIC_MEMORY_CARDS sync command`() = runTest {
        coEvery { geminiService.generateContent(any()) } returns
            "C: Célula | D: Unidade da vida\nC: DNA | D: Molécula da hereditariedade"

        repository.generateMemoryDeck(topic).toList()

        coVerify {
            syncCommandDispatcher.enqueue(
                SyncCommandType.UPSERT_TOPIC_MEMORY_CARDS,
                "1",
                match<UpsertTopicMemoryCardsPayload> { it.topicId == "1" },
            )
        }
    }

    @Test
    fun `generateMemoryDeck failure propagates and does not persist`() = runTest {
        coEvery { geminiService.generateContent(any()) } throws NetworkError.NoInternet()

        val results = repository.generateMemoryDeck(topic).toList()

        assertTrue(results.first().isFailure)
        assertIs<NetworkError.NoInternet>(results.first().exceptionOrNull())
        coVerify(exactly = 0) { topicDao.updateMemoryCards(any(), any(), any()) }
        coVerify(exactly = 0) {
            syncCommandDispatcher.enqueue(SyncCommandType.UPSERT_TOPIC_MEMORY_CARDS, any(), any())
        }
    }
}
