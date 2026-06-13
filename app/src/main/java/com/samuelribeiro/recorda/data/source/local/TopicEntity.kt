package com.samuelribeiro.recorda.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Offline-first storage for a study topic.
 *
 * Flashcards, mind map, study guide and chapter content are stored as JSON-serialized
 * strings rather than separate related tables — a single flat entity keeps the example
 * simple.
 */
@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: String,
    val name: String,
    val flashcardsJson: String,
    val mindMapJson: String? = null,
    val studyGuideJson: String? = null,
    val contentJson: String? = null,
)
