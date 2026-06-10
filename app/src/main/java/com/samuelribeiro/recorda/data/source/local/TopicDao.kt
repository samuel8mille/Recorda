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

    @Query("SELECT * FROM topics WHERE status = 'PENDING'")
    suspend fun getPending(): List<TopicEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TopicEntity)

    /** Emits the [TopicEntity] with the given [id], or `null` if not found. */
    @Query("SELECT * FROM topics WHERE id = :id")
    fun getById(id: String): Flow<TopicEntity?>

    @Query("UPDATE topics SET status = 'DONE', flashcardsJson = :flashcardsJson WHERE id = :id")
    suspend fun markDone(id: String, flashcardsJson: String)

    /** Persists the generated mind map for the topic with [id]. */
    @Query("UPDATE topics SET mindMapJson = :mindMapJson WHERE id = :id")
    suspend fun updateMindMap(id: String, mindMapJson: String)

    /** Deletes the topic with [id] from the database. */
    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteById(id: String)
}
