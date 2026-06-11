package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.StudyGuide
import com.samuelribeiro.recorda.domain.model.StudySection
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.StudyGuideRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GenerateStudyGuideUseCaseTest {

    private val repository: StudyGuideRepository = mockk()
    private val useCase = GenerateStudyGuideUseCase(repository)

    @Test
    fun `delegates to repository`() = runTest {
        val topic = Topic(id = "1", name = "Kotlin", flashcards = emptyList())
        val guide = StudyGuide(
            sections = listOf(StudySection(id = "0", title = "T", emoji = "✨", summary = "s", keyPoints = emptyList())),
        )
        every { repository.generateStudyGuide(topic) } returns flowOf(Result.success(guide))

        val result = useCase(topic).first()

        assertEquals(guide, result.getOrThrow())
    }
}
