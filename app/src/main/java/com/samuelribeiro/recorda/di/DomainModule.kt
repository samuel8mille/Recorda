package com.samuelribeiro.recorda.di

import com.samuelribeiro.recorda.domain.repository.MindMapRepository
import com.samuelribeiro.recorda.domain.repository.OralTestRepository
import com.samuelribeiro.recorda.domain.repository.ReviewRepository
import com.samuelribeiro.recorda.domain.repository.StatsRepository
import com.samuelribeiro.recorda.domain.repository.StudyGuideRepository
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.samuelribeiro.recorda.domain.scheduler.ReviewScheduler
import com.samuelribeiro.recorda.domain.stats.TopicStatsCalculator
import com.samuelribeiro.recorda.domain.usecase.CreateTopicUseCase
import com.samuelribeiro.recorda.domain.usecase.DeleteTopicUseCase
import com.samuelribeiro.recorda.domain.usecase.EvaluateOralAnswerUseCase
import com.samuelribeiro.recorda.domain.usecase.GenerateFlashcardsFromContentUseCase
import com.samuelribeiro.recorda.domain.usecase.GenerateMindMapUseCase
import com.samuelribeiro.recorda.domain.usecase.GenerateStudyGuideUseCase
import com.samuelribeiro.recorda.domain.usecase.GetFlashcardReviewsUseCase
import com.samuelribeiro.recorda.domain.usecase.GetStoredTopicsUseCase
import com.samuelribeiro.recorda.domain.usecase.GetTopicStatsUseCase
import com.samuelribeiro.recorda.domain.usecase.GetTopicUseCase
import com.samuelribeiro.recorda.domain.usecase.UpdateCardScheduleUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

/** Hilt module providing domain-layer dependencies. */
@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    /** Provides [DeleteTopicUseCase]. */
    @Provides
    fun provideDeleteTopicUseCase(
        topicRepository: TopicRepository,
        reviewRepository: ReviewRepository,
        statsRepository: StatsRepository,
    ): DeleteTopicUseCase = DeleteTopicUseCase(topicRepository, reviewRepository, statsRepository)

    /** Provides [CreateTopicUseCase]. */
    @Provides
    fun provideCreateTopicUseCase(
        repository: TopicRepository
    ): CreateTopicUseCase = CreateTopicUseCase(repository)

    /** Provides [GenerateFlashcardsFromContentUseCase]. */
    @Provides
    fun provideGenerateFlashcardsFromContentUseCase(
        repository: TopicRepository
    ): GenerateFlashcardsFromContentUseCase = GenerateFlashcardsFromContentUseCase(repository)

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
        statsRepository: StatsRepository,
    ): UpdateCardScheduleUseCase = UpdateCardScheduleUseCase(scheduler, repository, statsRepository)

    /** Provides [EvaluateOralAnswerUseCase]. */
    @Provides
    fun provideEvaluateOralAnswerUseCase(
        repository: OralTestRepository
    ): EvaluateOralAnswerUseCase = EvaluateOralAnswerUseCase(repository)

    /** Provides [GenerateMindMapUseCase]. */
    @Provides
    fun provideGenerateMindMapUseCase(
        repository: MindMapRepository
    ): GenerateMindMapUseCase = GenerateMindMapUseCase(repository)

    /** Provides [GenerateStudyGuideUseCase]. */
    @Provides
    fun provideGenerateStudyGuideUseCase(
        repository: StudyGuideRepository
    ): GenerateStudyGuideUseCase = GenerateStudyGuideUseCase(repository)

    /** Provides the system [Clock] used for day-based statistics. */
    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    /** Provides [TopicStatsCalculator]. */
    @Provides
    fun provideTopicStatsCalculator(clock: Clock): TopicStatsCalculator =
        TopicStatsCalculator(clock)

    /** Provides [GetTopicStatsUseCase]. */
    @Provides
    fun provideGetTopicStatsUseCase(
        topicRepository: TopicRepository,
        reviewRepository: ReviewRepository,
        statsRepository: StatsRepository,
        calculator: TopicStatsCalculator,
    ): GetTopicStatsUseCase =
        GetTopicStatsUseCase(topicRepository, reviewRepository, statsRepository, calculator)
}
