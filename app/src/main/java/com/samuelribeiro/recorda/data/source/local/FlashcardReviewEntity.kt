package com.samuelribeiro.recorda.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.samuelribeiro.recorda.domain.model.FlashcardReviewState

/** Room entity tracking the SM-2 scheduling state for a single flashcard. */
@Entity(tableName = "flashcard_reviews")
data class FlashcardReviewEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val cardIndex: Int,
    val easeFactor: Float = FlashcardReviewState.DEFAULT_EASE_FACTOR,
    val intervalDays: Int = 1,
    val repetitions: Int = 0,
    val nextReviewAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L,
)
