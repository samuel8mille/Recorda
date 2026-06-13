package com.samuelribeiro.recorda.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase

/** Room database for the Recorda app. */
@Database(
    entities = [TopicEntity::class, FlashcardReviewEntity::class, ReviewLogEntity::class],
    version = 7,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    /** Provides access to topic-related database operations. */
    abstract fun topicDao(): TopicDao

    /** Provides access to flashcard review scheduling state. */
    abstract fun flashcardReviewDao(): FlashcardReviewDao

    /** Provides access to the append-only review event log. */
    abstract fun reviewLogDao(): ReviewLogDao
}
