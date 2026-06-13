package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.Chapter
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.model.TopicContent
import com.samuelribeiro.recorda.domain.model.TopicContentStep
import io.mockk.mockk
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EnsureTopicContentUseCaseTest {

    private val generate: GenerateTopicContentUseCase = mockk()
    private val useCase = EnsureTopicContentUseCase(generate)

    private fun completeContent() = TopicContent(
        listOf(Chapter("0", "Intro", "Resumo", "Corpo completo")),
    )

    @Test
    fun `emits Completed without generating when content already complete`() = runTest {
        val topic = Topic("1", "Kotlin", emptyList(), content = completeContent())

        val results = useCase(topic).toList()

        assertEquals(1, results.size)
        val step = results.first().getOrThrow()
        assertIs<TopicContentStep.Completed>(step)
        assertEquals(completeContent(), step.content)
        verify(exactly = 0) { generate(any()) }
    }

    @Test
    fun `delegates to generate when content is missing`() = runTest {
        val topic = Topic("1", "Kotlin", emptyList(), content = null)
        every { generate(topic) } returns flowOf(Result.success(TopicContentStep.Completed(completeContent())))

        useCase(topic).toList()

        verify(exactly = 1) { generate(topic) }
    }

    @Test
    fun `delegates to generate when content is incomplete`() = runTest {
        val incomplete = TopicContent(listOf(Chapter("0", "Intro", "Resumo", body = "")))
        val topic = Topic("1", "Kotlin", emptyList(), content = incomplete)
        every { generate(topic) } returns flowOf(Result.success(TopicContentStep.Completed(completeContent())))

        useCase(topic).toList()

        verify(exactly = 1) { generate(topic) }
    }
}
