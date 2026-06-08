package com.samuelribeiro.recorda.di

import com.samuelribeiro.recorda.domain.repository.TopicRepository
import com.samuelribeiro.recorda.domain.usecase.GenerateFlashcardsUseCase
import com.samuelribeiro.recorda.domain.usecase.GetStoredTopicsUseCase
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
}
