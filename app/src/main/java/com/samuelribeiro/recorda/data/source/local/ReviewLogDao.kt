package com.samuelribeiro.recorda.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/** Data access for the append-only review event log. */
@Dao
interface ReviewLogDao {

    /** Appends a review event to the log. */
    @Insert
    suspend fun insert(entity: ReviewLogEntity)

    /** Returns all review events of the topic with [topicId], oldest first. */
    @Query("SELECT * FROM review_logs WHERE topicId = :topicId ORDER BY timestampMillis ASC")
    suspend fun getLogsForTopic(topicId: String): List<ReviewLogEntity>

    /** Deletes all review events of the topic with [topicId]. */
    @Query("DELETE FROM review_logs WHERE topicId = :topicId")
    suspend fun deleteByTopicId(topicId: String)
}
