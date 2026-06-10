package com.samuelribeiro.recorda.data.repository

import com.samuelribeiro.recorda.core.network.NetworkError
import com.samuelribeiro.recorda.core.network.ServiceExecutor
import com.samuelribeiro.recorda.data.mapper.OralAnswerMapper
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.domain.model.OralAnswerVerdict
import com.samuelribeiro.recorda.domain.prompt.OralAnswerPromptBuilder
import com.samuelribeiro.recorda.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OralTestRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val geminiService: GeminiService = mockk()
    private val oralAnswerMapper = OralAnswerMapper()
    private val promptBuilder: OralAnswerPromptBuilder = mockk {
        every { build(any(), any(), any()) } returns "prompt"
    }

    private val serviceExecutor by lazy {
        ServiceExecutor(ioDispatcher = mainDispatcherRule.testDispatcher)
    }

    private val repository by lazy {
        OralTestRepositoryImpl(
            geminiService = geminiService,
            oralAnswerMapper = oralAnswerMapper,
            serviceExecutor = serviceExecutor,
            promptBuilder = promptBuilder,
        )
    }

    @Test
    fun `evaluateAnswer success maps Gemini response to evaluation`() = runTest {
        coEvery { geminiService.generateContent(any()) } returns "VEREDITO: CORRECT | FEEDBACK: Muito bem!"

        val results = repository.evaluateAnswer("Pergunta?", "Resposta esperada", "Resposta falada").toList()

        val evaluation = results.first().getOrThrow()
        assertEquals(OralAnswerVerdict.CORRECT, evaluation.verdict)
        assertEquals("Muito bem!", evaluation.feedback)
    }

    @Test
    fun `evaluateAnswer builds prompt from question, expected and spoken answers`() = runTest {
        coEvery { geminiService.generateContent(any()) } returns "VEREDITO: CORRECT | FEEDBACK: Ok"

        repository.evaluateAnswer("Pergunta?", "Resposta esperada", "Resposta falada").toList()

        verify { promptBuilder.build("Pergunta?", "Resposta esperada", "Resposta falada") }
    }

    @Test
    fun `evaluateAnswer failure propagates network error`() = runTest {
        coEvery { geminiService.generateContent(any()) } throws NetworkError.NoInternet()

        val results = repository.evaluateAnswer("Pergunta?", "Resposta esperada", "Resposta falada").toList()

        assertTrue(results.first().isFailure)
        assertIs<NetworkError.NoInternet>(results.first().exceptionOrNull())
    }
}
