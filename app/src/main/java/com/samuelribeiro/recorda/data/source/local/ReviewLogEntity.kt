package com.samuelribeiro.recorda.data.source.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Append-only log of card review events, the raw data behind retention statistics.
 *
 * Unlike [FlashcardReviewEntity] (one row of *current* SM-2 state per card), each
 * review of a card appends a new row here, so the primary key is auto-generated.
 *
 * @property id Auto-generated row id.
 * @property topicId The topic the reviewed card belongs to.
 * @property cardIndex Index of the reviewed card within the topic.
 * @property rating Name of the [com.samuelribeiro.recorda.domain.model.CardRating] given by
 * the user — stored as text so reordering the enum never corrupts old rows.
 * @property timestampMillis Epoch millis (UTC) of when the review happened.
 */
@Entity(tableName = "review_logs", indices = [Index("topicId")])
data class ReviewLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val topicId: String,
    val cardIndex: Int,
    val rating: String,
    val timestampMillis: Long,
)
