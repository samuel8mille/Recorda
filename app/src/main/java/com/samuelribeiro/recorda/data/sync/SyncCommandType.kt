package com.samuelribeiro.recorda.data.sync

/** Types of local mutations queued for upload to the sync backend. */
enum class SyncCommandType {
    CREATE_TOPIC,
    DELETE_TOPIC,
    SAVE_REVIEW_STATE,
    LOG_REVIEW,
    DELETE_REVIEW_LOG,
    UPSERT_TOPIC_FLASHCARDS,
    UPSERT_TOPIC_MIND_MAP,
    UPSERT_TOPIC_CONTENT,
    UPSERT_TOPIC_MEMORY_CARDS,
}
