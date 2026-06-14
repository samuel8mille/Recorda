package com.samuelribeiro.recorda.data.repository

import com.google.gson.Gson
import com.samuelribeiro.recorda.core.network.NetworkError
import com.samuelribeiro.recorda.core.network.ServiceExecutor
import com.samuelribeiro.recorda.data.mapper.MindMapMapper
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.data.sync.SyncCommandDispatcher
import com.samuelribeiro.recorda.data.sync.SyncCommandType
import com.samuelribeiro.recorda.data.sync.UpsertTopicMindMapPayload
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.prompt.MindMapPromptBuilder
import com.samuelribeiro.recorda.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
class MindMapRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val geminiService: GeminiService = mockk()
    private val mindMapMapper = MindMapMapper()
    private val topicDao: TopicDao = mockk(relaxed = true)
    private val gson = Gson()
    private val promptBuilder: MindMapPromptBuilder = mockk {
        every { build(any(), any()) } returns "prompt"
    }
    private val syncCommandDispatcher: SyncCommandDispatcher = mockk(relaxed = true)

    private val serviceExecutor by lazy {
        ServiceExecutor(ioDispatcher = mainDispatcherRule.testDispatcher)
    }

    private val repository by lazy {
        MindMapRepositoryImpl(
            geminiService = geminiService,
            mindMapMapper = mindMapMapper,
            serviceExecutor = serviceExecutor,
            topicDao = topicDao,
            gson = gson,
            promptBuilder = promptBuilder,
            syncCommandDispatcher = syncCommandDispatcher,
        )
    }

    private val topic = Topic(
        id = "1",
        name = "Kotlin",
        flashcards = listOf(Flashcard("What is Kotlin?", "A JVM language")),
    )

    @Test
    fun `generateMindMap success maps response and persists json`() = runTest {
        coEvery { geminiService.generateContent(any()) } returns "Kotlin\n- Sintaxe\n- Coroutines"

        val results = repository.generateMindMap(topic).toList()

        val node = results.first().getOrThrow()
        assertEquals("Kotlin", node.title)
        assertEquals(listOf("Sintaxe", "Coroutines"), node.children.map { it.title })
        coVerify { topicDao.updateMindMap("1", gson.toJson(node), any()) }
    }

    @Test
    fun `generateMindMap success enqueues an UPSERT_TOPIC_MIND_MAP sync command`() = runTest {
        coEvery { geminiService.generateContent(any()) } returns "Kotlin\n- Sintaxe\n- Coroutines"

        repository.generateMindMap(topic).toList()

        coVerify {
            syncCommandDispatcher.enqueue(
                SyncCommandType.UPSERT_TOPIC_MIND_MAP,
                "1",
                match<UpsertTopicMindMapPayload> { it.topicId == "1" },
            )
        }
    }

    @Test
    fun `generateMindMap failure propagates network error and does not persist`() = runTest {
        coEvery { geminiService.generateContent(any()) } throws NetworkError.NoInternet()

        val results = repository.generateMindMap(topic).toList()

        assertTrue(results.first().isFailure)
        assertIs<NetworkError.NoInternet>(results.first().exceptionOrNull())
        coVerify(exactly = 0) { topicDao.updateMindMap(any(), any(), any()) }
        coVerify(exactly = 0) {
            syncCommandDispatcher.enqueue(SyncCommandType.UPSERT_TOPIC_MIND_MAP, any(), any())
        }
    }
}
