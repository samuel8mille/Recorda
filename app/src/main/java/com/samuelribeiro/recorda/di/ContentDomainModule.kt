package com.samuelribeiro.recorda.di

import com.samuelribeiro.recorda.domain.repository.TopicContentRepository
import com.samuelribeiro.recorda.domain.usecase.EnsureTopicContentUseCase
import com.samuelribeiro.recorda.domain.usecase.GenerateTopicContentUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Hilt module providing the chapter-content domain use cases. */
@Module
@InstallIn(SingletonComponent::class)
object ContentDomainModule {

    /** Provides [GenerateTopicContentUseCase]. */
    @Provides
    fun provideGenerateTopicContentUseCase(
        repository: TopicContentRepository
    ): GenerateTopicContentUseCase = GenerateTopicContentUseCase(repository)

    /** Provides [EnsureTopicContentUseCase]. */
    @Provides
    fun provideEnsureTopicContentUseCase(
        generate: GenerateTopicContentUseCase
    ): EnsureTopicContentUseCase = EnsureTopicContentUseCase(generate)
}
