package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class CreateTopicUseCaseTest {

    private val repository: TopicRepository = mockk()
    private val useCase = CreateTopicUseCase(repository)

    @Test
    fun `invoke delegates to repository and returns created topic`() = runTest {
        val topic = Topic("1", "Kotlin", emptyList())
        coEvery { repository.createTopic("Kotlin") } returns topic

        val result = useCase("Kotlin")

        assertEquals(topic, result)
        coVerify(exactly = 1) { repository.createTopic("Kotlin") }
    }
}
