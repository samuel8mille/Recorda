package com.samuelribeiro.recorda.data.repository

import com.samuelribeiro.recorda.data.source.local.FlashcardReviewDao
import com.samuelribeiro.recorda.data.source.local.FlashcardReviewEntity
import com.samuelribeiro.recorda.data.sync.SaveReviewStatePayload
import com.samuelribeiro.recorda.data.sync.SyncCommandDispatcher
import com.samuelribeiro.recorda.data.sync.SyncCommandType
import com.samuelribeiro.recorda.domain.model.FlashcardReviewState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class ReviewRepositoryImplTest {

    private val dao: FlashcardReviewDao = mockk()
    private val syncCommandDispatcher: SyncCommandDispatcher = mockk(relaxed = true)
    private val repository = ReviewRepositoryImpl(dao, syncCommandDispatcher)

    @Test
    fun `getReviewStates maps entities to domain models`() = runTest {
        coEvery { dao.getReviewsForTopic("topic1") } returns listOf(
            FlashcardReviewEntity(
                id = "topic1_0",
                topicId = "topic1",
                cardIndex = 0,
                easeFactor = 2.1f,
                intervalDays = 3,
                repetitions = 2,
                nextReviewAtMillis = 1_000_000L,
            )
        )

        val result = repository.getReviewStates("topic1")

        assertEquals(1, result.size)
        assertEquals(0, result[0].cardIndex)
        assertEquals(2.1f, result[0].easeFactor)
        assertEquals(3, result[0].intervalDays)
        assertEquals(2, result[0].repetitions)
        assertEquals(1_000_000L, result[0].nextReviewAtMillis)
    }

    @Test
    fun `getReviewStates returns empty list when no reviews exist`() = runTest {
        coEvery { dao.getReviewsForTopic("topic1") } returns emptyList()

        val result = repository.getReviewStates("topic1")

        assertEquals(emptyList(), result)
    }

    @Test
    fun `saveReviewState upserts entity with composite id`() = runTest {
        val entitySlot = slot<FlashcardReviewEntity>()
        coEvery { dao.upsert(capture(entitySlot)) } returns Unit

        repository.saveReviewState(
            topicId = "topic1",
            state = FlashcardReviewState(
                cardIndex = 2,
                easeFactor = 2.5f,
                intervalDays = 6,
                repetitions = 1,
                nextReviewAtMillis = 5_000L,
            )
        )

        val entity = entitySlot.captured
        assertEquals("topic1_2", entity.id)
        assertEquals("topic1", entity.topicId)
        assertEquals(2, entity.cardIndex)
        assertEquals(2.5f, entity.easeFactor)
        assertEquals(6, entity.intervalDays)
        assertEquals(1, entity.repetitions)
        assertEquals(5_000L, entity.nextReviewAtMillis)
    }

    @Test
    fun `saveReviewState calls dao upsert once`() = runTest {
        coEvery { dao.upsert(any()) } returns Unit

        repository.saveReviewState(
            topicId = "topic1",
            state = FlashcardReviewState(cardIndex = 0),
        )

        coVerify(exactly = 1) { dao.upsert(any()) }
    }

    @Test
    fun `saveReviewState enqueues a SAVE_REVIEW_STATE sync command with composite id`() = runTest {
        coEvery { dao.upsert(any()) } returns Unit

        repository.saveReviewState(
            topicId = "topic1",
            state = FlashcardReviewState(cardIndex = 2),
        )

        coVerify {
            syncCommandDispatcher.enqueue(
                SyncCommandType.SAVE_REVIEW_STATE,
                "topic1_2",
                match<SaveReviewStatePayload> { it.topicId == "topic1" && it.cardIndex == 2 },
            )
        }
    }

    @Test
    fun `deleteReviewStates delegates to dao deleteByTopicId`() = runTest {
        coEvery { dao.deleteByTopicId(any()) } returns Unit

        repository.deleteReviewStates("topic1")

        coVerify(exactly = 1) { dao.deleteByTopicId("topic1") }
    }
}
