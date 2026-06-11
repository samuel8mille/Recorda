package com.samuelribeiro.recorda.data.repository

import com.google.gson.Gson
import com.samuelribeiro.recorda.core.network.NetworkError
import com.samuelribeiro.recorda.core.network.ServiceExecutor
import com.samuelribeiro.recorda.data.mapper.StudyGuideMapper
import com.samuelribeiro.recorda.data.mapper.StudyGuideParseException
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.data.source.remote.service.WikipediaImageService
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.prompt.StudyGuidePromptBuilder
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StudyGuideRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val geminiService: GeminiService = mockk()
    private val wikipediaImageService: WikipediaImageService = mockk()
    private val studyGuideMapper = StudyGuideMapper(Gson())
    private val topicDao: TopicDao = mockk(relaxed = true)
    private val gson = Gson()
    private val promptBuilder: StudyGuidePromptBuilder = mockk {
        every { build(any()) } returns "prompt"
    }

    private val serviceExecutor by lazy {
        ServiceExecutor(ioDispatcher = mainDispatcherRule.testDispatcher)
    }

    private val repository by lazy {
        StudyGuideRepositoryImpl(
            geminiService = geminiService,
            studyGuideMapper = studyGuideMapper,
            wikipediaImageService = wikipediaImageService,
            serviceExecutor = serviceExecutor,
            topicDao = topicDao,
            gson = gson,
            promptBuilder = promptBuilder,
        )
    }

    private val topic = Topic(
        id = "1",
        name = "Kotlin",
        flashcards = listOf(Flashcard("O que é Kotlin?", "Linguagem JVM")),
    )

    private val guideJson =
        """{"sections":[{"title":"Sintaxe","emoji":"📝","summary":"s","keyPoints":["k"]}]}"""

    @Test
    fun `success enriches sections with image url and caches json`() = runTest {
        coEvery { geminiService.generateContent(any()) } returns guideJson
        coEvery { wikipediaImageService.findImageUrl("Sintaxe") } returns "https://img/s.jpg"

        val guide = repository.generateStudyGuide(topic).toList().first().getOrThrow()

        assertEquals("https://img/s.jpg", guide.sections[0].imageUrl)
        coVerify { topicDao.updateStudyGuide("1", gson.toJson(guide)) }
    }

    @Test
    fun `image lookup failure does not fail generation`() = runTest {
        coEvery { geminiService.generateContent(any()) } returns guideJson
        coEvery { wikipediaImageService.findImageUrl(any()) } throws NetworkError.HttpError(500, "boom")

        val guide = repository.generateStudyGuide(topic).toList().first().getOrThrow()

        assertNull(guide.sections[0].imageUrl)
        coVerify { topicDao.updateStudyGuide("1", any()) }
    }

    @Test
    fun `image lookup without result keeps null url`() = runTest {
        coEvery { geminiService.generateContent(any()) } returns guideJson
        coEvery { wikipediaImageService.findImageUrl(any()) } returns null

        val guide = repository.generateStudyGuide(topic).toList().first().getOrThrow()

        assertNull(guide.sections[0].imageUrl)
    }

    @Test
    fun `gemini failure propagates and does not cache`() = runTest {
        coEvery { geminiService.generateContent(any()) } throws NetworkError.NoInternet()

        val results = repository.generateStudyGuide(topic).toList()

        assertTrue(results.first().isFailure)
        assertIs<NetworkError.NoInternet>(results.first().exceptionOrNull())
        coVerify(exactly = 0) { topicDao.updateStudyGuide(any(), any()) }
    }

    @Test
    fun `unparseable response fails with parse exception and does not cache`() = runTest {
        coEvery { geminiService.generateContent(any()) } returns "resposta sem json"

        val results = repository.generateStudyGuide(topic).toList()

        assertTrue(results.first().isFailure)
        assertIs<StudyGuideParseException>(results.first().exceptionOrNull())
        coVerify(exactly = 0) { topicDao.updateStudyGuide(any(), any()) }
    }
}
