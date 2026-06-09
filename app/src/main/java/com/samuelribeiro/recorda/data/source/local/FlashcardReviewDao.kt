package com.samuelribeiro.recorda.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/** Room DAO for reading and writing [FlashcardReviewEntity] records. */
@Dao
interface FlashcardReviewDao {

    /** Returns all review states for cards belonging to [topicId]. */
    @Query("SELECT * FROM flashcard_reviews WHERE topicId = :topicId")
    suspend fun getReviewsForTopic(topicId: String): List<FlashcardReviewEntity>

    /** Inserts or replaces a [FlashcardReviewEntity]. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FlashcardReviewEntity)

    /** Deletes all review states whose [FlashcardReviewEntity.topicId] matches [topicId]. */
    @Query("DELETE FROM flashcard_reviews WHERE topicId = :topicId")
    suspend fun deleteByTopicId(topicId: String)
}
