package com.samuelribeiro.recorda.domain.usecase

import com.samuelribeiro.recorda.domain.model.FlashcardReviewState
import com.samuelribeiro.recorda.domain.repository.ReviewRepository

/** Loads all saved SM-2 scheduling states for a topic's flashcards. */
class GetFlashcardReviewsUseCase(private val repository: ReviewRepository) {

    /** Returns the persisted [FlashcardReviewState] list for [topicId]. */
    suspend operator fun invoke(topicId: String): List<FlashcardReviewState> =
        repository.getReviewStates(topicId)
}
