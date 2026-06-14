package com.samuelribeiro.recorda.data.repository

import com.samuelribeiro.recorda.data.source.local.ReviewLogDao
import com.samuelribeiro.recorda.data.source.local.ReviewLogEntity
import com.samuelribeiro.recorda.data.sync.DeleteReviewLogPayload
import com.samuelribeiro.recorda.data.sync.LogReviewPayload
import com.samuelribeiro.recorda.data.sync.SyncCommandDispatcher
import com.samuelribeiro.recorda.data.sync.SyncCommandType
import com.samuelribeiro.recorda.domain.model.CardRating
import com.samuelribeiro.recorda.domain.model.ReviewLogEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class StatsRepositoryImplTest {

    private val reviewLogDao: ReviewLogDao = mockk(relaxed = true)
    private val syncCommandDispatcher: SyncCommandDispatcher = mockk(relaxed = true)
    private val repository = StatsRepositoryImpl(reviewLogDao, syncCommandDispatcher)

    @Test
    fun `logReview persists entity with rating name`() = runTest {
        repository.logReview("topic1", ReviewLogEntry(cardIndex = 3, rating = CardRating.EASY, timestampMillis = 123L))

        coVerify {
            reviewLogDao.insert(
                match {
                    it.topicId == "topic1" && it.cardIndex == 3 && it.rating == "EASY" && it.timestampMillis == 123L
                },
            )
        }
    }

    @Test
    fun `logReview enqueues a LOG_REVIEW sync command`() = runTest {
        repository.logReview("topic1", ReviewLogEntry(cardIndex = 3, rating = CardRating.EASY, timestampMillis = 123L))

        coVerify {
            syncCommandDispatcher.enqueue(
                SyncCommandType.LOG_REVIEW,
                any(),
                match<LogReviewPayload> {
                    it.topicId == "topic1" && it.cardIndex == 3 && it.rating == "EASY" && it.timestampMillis == 123L
                },
            )
        }
    }

    @Test
    fun `getReviewLog maps entities back to domain with rating round-trip`() = runTest {
        coEvery { reviewLogDao.getLogsForTopic("topic1") } returns listOf(
            ReviewLogEntity(id = 1, topicId = "topic1", cardIndex = 0, rating = "AGAIN", timestampMillis = 10L),
            ReviewLogEntity(id = 2, topicId = "topic1", cardIndex = 1, rating = "GOOD", timestampMillis = 20L),
        )

        val log = repository.getReviewLog("topic1")

        assertEquals(
            listOf(
                ReviewLogEntry(cardIndex = 0, rating = CardRating.AGAIN, timestampMillis = 10L),
                ReviewLogEntry(cardIndex = 1, rating = CardRating.GOOD, timestampMillis = 20L),
            ),
            log,
        )
    }

    @Test
    fun `deleteReviewLog delegates to dao`() = runTest {
        repository.deleteReviewLog("topic1")

        coVerify { reviewLogDao.deleteByTopicId("topic1") }
    }

    @Test
    fun `deleteReviewLog enqueues a DELETE_REVIEW_LOG sync command`() = runTest {
        repository.deleteReviewLog("topic1")

        coVerify {
            syncCommandDispatcher.enqueue(
                SyncCommandType.DELETE_REVIEW_LOG,
                "topic1",
                DeleteReviewLogPayload("topic1"),
            )
        }
    }
}
