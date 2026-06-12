package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.CardRating
import com.samuelribeiro.recorda.domain.model.Flashcard
import com.samuelribeiro.recorda.domain.model.FlashcardReviewState
import com.samuelribeiro.recorda.domain.model.ReviewLogEntry
import com.samuelribeiro.recorda.domain.model.Topic
import com.samuelribeiro.recorda.domain.repository.ReviewRepository
import com.samuelribeiro.recorda.domain.repository.StatsRepository
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.samuelribeiro.recorda.domain.stats.TopicStatsCalculator
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetTopicStatsUseCaseTest {

    private val topicRepository: TopicRepository = mockk()
    private val reviewRepository: ReviewRepository = mockk()
    private val statsRepository: StatsRepository = mockk()
    private val clock = Clock.fixed(Instant.parse("2026-06-10T15:00:00Z"), ZoneId.of("America/Sao_Paulo"))
    private val useCase = GetTopicStatsUseCase(
        topicRepository = topicRepository,
        reviewRepository = reviewRepository,
        statsRepository = statsRepository,
        calculator = TopicStatsCalculator(clock),
    )

    private val topic = Topic(
        id = "topic1",
        name = "Kotlin",
        flashcards = listOf(Flashcard("Q1?", "A1"), Flashcard("Q2?", "A2")),
    )

    @Test
    fun `aggregates data from the three repositories`() = runTest {
        every { topicRepository.getTopic("topic1") } returns flowOf(topic)
        coEvery { reviewRepository.getReviewStates("topic1") } returns listOf(
            FlashcardReviewState(cardIndex = 0, easeFactor = 2.0f, nextReviewAtMillis = clock.millis() + 1),
        )
        coEvery { statsRepository.getReviewLog("topic1") } returns listOf(
            ReviewLogEntry(cardIndex = 0, rating = CardRating.GOOD, timestampMillis = clock.millis()),
        )

        val stats = useCase("topic1")

        assertEquals(2, stats.totalCards)
        assertEquals(1, stats.cardsOnTrack)
        assertEquals(1, stats.cardsNeverReviewed)
        assertEquals(1.0f, stats.successRate)
        assertEquals(2.0f, stats.averageEaseFactor)
        assertEquals(1, stats.streakDays)
    }

    @Test
    fun `missing topic yields zero cards`() = runTest {
        every { topicRepository.getTopic("missing") } returns flowOf(null)
        coEvery { reviewRepository.getReviewStates("missing") } returns emptyList()
        coEvery { statsRepository.getReviewLog("missing") } returns emptyList()

        val stats = useCase("missing")

        assertEquals(0, stats.totalCards)
        assertNull(stats.successRate)
    }
}
