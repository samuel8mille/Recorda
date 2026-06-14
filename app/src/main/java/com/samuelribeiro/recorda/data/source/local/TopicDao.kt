package com.samuelribeiro.recorda.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY rowid DESC")
    fun getAll(): Flow<List<TopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TopicEntity)

    /** Emits the [TopicEntity] with the given [id], or `null` if not found. */
    @Query("SELECT * FROM topics WHERE id = :id")
    fun getById(id: String): Flow<TopicEntity?>

    /** Persists the generated flashcards for the topic with [id]. */
    @Query("UPDATE topics SET flashcardsJson = :flashcardsJson, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateFlashcards(id: String, flashcardsJson: String, updatedAtMillis: Long)

    /** Persists the generated mind map for the topic with [id]. */
    @Query("UPDATE topics SET mindMapJson = :mindMapJson, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateMindMap(id: String, mindMapJson: String, updatedAtMillis: Long)

    /** Persists the (possibly partial) chapter content for the topic with [id]. */
    @Query("UPDATE topics SET contentJson = :contentJson, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateContent(id: String, contentJson: String, updatedAtMillis: Long)

    /** Persists the generated active-recall deck for the topic with [id]. */
    @Query("UPDATE topics SET memoryCardsJson = :memoryCardsJson, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateMemoryCards(id: String, memoryCardsJson: String, updatedAtMillis: Long)

    /** Deletes the topic with [id] from the database. */
    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteById(id: String)
}
