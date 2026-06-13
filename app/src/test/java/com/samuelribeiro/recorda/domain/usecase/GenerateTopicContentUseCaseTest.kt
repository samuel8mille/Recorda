package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.Chapter
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.model.TopicContent
import com.samuelribeiro.recorda.domain.model.TopicContentStep
import com.samuelribeiro.recorda.domain.repository.TopicContentRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GenerateTopicContentUseCaseTest {

    private val repository: TopicContentRepository = mockk()
    private val useCase = GenerateTopicContentUseCase(repository)
    private val topic = Topic("1", "Kotlin", emptyList())

    @Test
    fun `invoke delegates to repository`() = runTest {
        val content = TopicContent(listOf(Chapter("0", "Intro", "Resumo", "Corpo")))
        every { repository.generateTopicContent(topic) } returns
            flowOf(Result.success(TopicContentStep.Completed(content)))

        val results = useCase(topic).toList()

        verify(exactly = 1) { repository.generateTopicContent(topic) }
        assertEquals(content, results.first().getOrThrow().content)
    }
}
