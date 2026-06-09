package com.samuelribeiro.recorda.di

import com.samuelribeiro.recorda.domain.repository.ReviewRepository
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.samuelribeiro.recorda.domain.scheduler.ReviewScheduler
import com.samuelribeiro.recorda.domain.usecase.GenerateFlashcardsUseCase
import com.samuelribeiro.recorda.domain.usecase.GetFlashcardReviewsUseCase
import com.samuelribeiro.recorda.domain.usecase.GetStoredTopicsUseCase
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import com.samuelribeiro.recorda.domain.usecase.UpdateCardScheduleUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Hilt module providing domain-layer dependencies. */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    fun provideGenerateFlashcardsUseCase(
        repository: TopicRepository
    ): GenerateFlashcardsUseCase = GenerateFlashcardsUseCase(repository)

    @Provides
    fun provideGetStoredTopicsUseCase(
        repository: TopicRepository
    ): GetStoredTopicsUseCase = GetStoredTopicsUseCase(repository)

    /** Provides [GetTopicUseCase]. */
    @Provides
    fun provideGetTopicUseCase(repository: TopicRepository): GetTopicUseCase =
        GetTopicUseCase(repository)

    /** Provides [GetFlashcardReviewsUseCase]. */
    @Provides
    fun provideGetFlashcardReviewsUseCase(
        repository: ReviewRepository
    ): GetFlashcardReviewsUseCase = GetFlashcardReviewsUseCase(repository)

    /** Provides [UpdateCardScheduleUseCase]. */
    @Provides
    fun provideUpdateCardScheduleUseCase(
        scheduler: ReviewScheduler,
        repository: ReviewRepository,
    ): UpdateCardScheduleUseCase = UpdateCardScheduleUseCase(scheduler, repository)
}
