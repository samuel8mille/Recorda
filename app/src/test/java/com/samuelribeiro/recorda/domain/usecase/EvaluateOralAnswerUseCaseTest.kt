package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.OralAnswerEvaluation
import com.samuelribeiro.recorda.domain.model.OralAnswerVerdict
import com.samuelribeiro.recorda.domain.repository.OralTestRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class EvaluateOralAnswerUseCaseTest {

    private val repository: OralTestRepository = mockk()
    private val useCase = EvaluateOralAnswerUseCase(repository)

    @Test
    fun `invoke delegates to repository`() = runTest {
        val evaluation = OralAnswerEvaluation(OralAnswerVerdict.CORRECT, "Muito bem!")
        every { repository.evaluateAnswer("Q?", "A", "spoken") } returns flowOf(Result.success(evaluation))

        val results = useCase("Q?", "A", "spoken").toList()

        verify(exactly = 1) { repository.evaluateAnswer("Q?", "A", "spoken") }
        assertEquals(evaluation, results.first().getOrThrow())
    }
}
