package com.samuelribeiro.recorda.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TopicStatus { DONE, PENDING }

/**
 * Offline-first storage for a study topic.
 *
 * Flashcards are stored as a JSON-serialized string ([flashcardsJson]) rather than a
 * separate related table — a single flat entity keeps the example simple while still
 * mirroring [com.samuelribeiro.recorda.data.source.local] from the network-retry pattern
 * (status PENDING/DONE) used by UrlShortener's `ShortUrlEntity`.
 */
@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: String,
    val name: String,
    val flashcardsJson: String,
    val status: TopicStatus = TopicStatus.DONE,
    val mindMapJson: String? = null,
    val studyGuideJson: String? = null,
)
