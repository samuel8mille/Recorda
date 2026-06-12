package com.samuelribeiro.recorda.presentation.ui.study

import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.StudyGuide
import com.samuelribeiro.recorda.domain.model.StudySection
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.samuelribeiro.recorda.domain.usecase.GenerateStudyGuideUseCase
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import com.samuelribeiro.recorda.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class StudyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val topicRepository: TopicRepository = mockk()
    private val getTopicUseCase = GetTopicUseCase(topicRepository)
    private val generateStudyGuideUseCase: GenerateStudyGuideUseCase = mockk()

    private val topic = Topic(
        id = "topic1",
        name = "Kotlin",
        flashcards = listOf(Flashcard("O que é Kotlin?", "Linguagem JVM")),
    )

    private val guide = StudyGuide(
        sections = listOf(
            StudySection(
                id = "0",
                title = "Sintaxe",
                emoji = "📝",
                definition = "d",
                content = "c",
                summary = "s",
                keyPoints = listOf("k"),
            ),
            StudySection(
                id = "1",
                title = "Coroutines",
                emoji = "🧵",
                definition = "d2",
                content = "c2",
                summary = "s2",
                keyPoints = listOf("k2"),
            ),
        ),
    )

    private fun createViewModel(topicId: String = "topic1"): StudyViewModel =
        StudyViewModel(
            topicId = topicId,
            getTopicUseCase = getTopicUseCase,
            generateStudyGuideUseCase = generateStudyGuideUseCase,
        )

    @Test
    fun `init generates guide when topic has no cached guide`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        every { generateStudyGuideUseCase(topic) } returns flowOf(Result.success(guide))

        val vm = createViewModel()

        assertEquals("Kotlin", vm.stateFlow.value.content.topicName)
        assertEquals(guide, vm.stateFlow.value.content.guide)
        assertNull(vm.stateFlow.value.loading)
    }

    @Test
    fun `init uses cached guide without generating a new one`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic.copy(studyGuide = guide))

        val vm = createViewModel()

        assertEquals(guide, vm.stateFlow.value.content.guide)
        verify(exactly = 0) { generateStudyGuideUseCase(any()) }
    }

    @Test
    fun `SelectSection opens detail and CloseSection returns to list`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic.copy(studyGuide = guide))
        val vm = createViewModel()

        vm.onSendEvent(SelectSection("1"))
        assertEquals("1", vm.stateFlow.value.content.selectedSectionId)
        assertEquals(guide.sections[1], vm.stateFlow.value.content.selectedSection)

        vm.onSendEvent(CloseSection)
        assertNull(vm.stateFlow.value.content.selectedSectionId)
        assertNull(vm.stateFlow.value.content.selectedSection)
    }

    @Test
    fun `generation failure sets error and leaves guide null`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        every { generateStudyGuideUseCase(topic) } returns flowOf(Result.failure(Exception("boom")))

        val vm = createViewModel()

        assertNull(vm.stateFlow.value.content.guide)
        assertNotNull(vm.stateFlow.value.error)
    }

    @Test
    fun `topic not found leaves state with default empty values`() = runTest {
        every { topicRepository.getTopic("missing") } returns flowOf(null)

        val vm = createViewModel(topicId = "missing")

        assertEquals("", vm.stateFlow.value.content.topicName)
        assertNull(vm.stateFlow.value.content.guide)
    }

    @Test
    fun `selectedSection is null for unknown id`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic.copy(studyGuide = guide))
        val vm = createViewModel()

        vm.onSendEvent(SelectSection("99"))

        assertEquals("99", vm.stateFlow.value.content.selectedSectionId)
        assertNull(vm.stateFlow.value.content.selectedSection)
    }
}
