package com.samuelribeiro.recorda.data.sync

/** Payload for [SyncCommandType.CREATE_TOPIC]. */
data class CreateTopicPayload(val topicId: String, val name: String)

/** Payload for [SyncCommandType.DELETE_TOPIC]. */
data class DeleteTopicPayload(val topicId: String)

/** Payload for [SyncCommandType.SAVE_REVIEW_STATE]. */
data class SaveReviewStatePayload(
    val topicId: String,
    val cardIndex: Int,
    val easeFactor: Float,
    val intervalDays: Int,
    val repetitions: Int,
    val nextReviewAtMillis: Long,
    val updatedAtMillis: Long,
)

/** Payload for [SyncCommandType.LOG_REVIEW]. */
data class LogReviewPayload(
    val topicId: String,
    val cardIndex: Int,
    val rating: String,
    val timestampMillis: Long,
)

/** Payload for [SyncCommandType.DELETE_REVIEW_LOG]. */
data class DeleteReviewLogPayload(val topicId: String)

/** Payload for [SyncCommandType.UPSERT_TOPIC_FLASHCARDS]. */
data class UpsertTopicFlashcardsPayload(
    val topicId: String,
    val flashcardsJson: String,
    val updatedAtMillis: Long,
)

/** Payload for [SyncCommandType.UPSERT_TOPIC_MIND_MAP]. */
data class UpsertTopicMindMapPayload(
    val topicId: String,
    val mindMapJson: String,
    val updatedAtMillis: Long,
)

/** Payload for [SyncCommandType.UPSERT_TOPIC_CONTENT]. */
data class UpsertTopicContentPayload(
    val topicId: String,
    val contentJson: String,
    val updatedAtMillis: Long,
)

/** Payload for [SyncCommandType.UPSERT_TOPIC_MEMORY_CARDS]. */
data class UpsertTopicMemoryCardsPayload(
    val topicId: String,
    val memoryCardsJson: String,
    val updatedAtMillis: Long,
)
