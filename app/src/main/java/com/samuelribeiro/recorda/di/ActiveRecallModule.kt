package com.samuelribeiro.recorda.di

import com.samuelribeiro.recorda.data.prompt.GeminiMemoryDeckPromptBuilder
import com.samuelribeiro.recorda.data.repository.MemoryDeckRepositoryImpl
import com.samuelribeiro.recorda.domain.prompt.MemoryDeckPromptBuilder
import com.samuelribeiro.recorda.domain.repository.MemoryDeckRepository
import com.samuelribeiro.recorda.domain.usecase.EnsureMemoryDeckUseCase
import com.samuelribeiro.recorda.domain.usecase.EnsureTopicContentUseCase
import com.samuelribeiro.recorda.domain.usecase.GenerateMemoryDeckUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing the active-recall feature dependencies: the deck repository and prompt
 * builder bindings plus the deck use cases (kept separate to stay under the detekt
 * TooManyFunctions threshold on [DomainModule]).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ActiveRecallModule {

    /** Binds [MemoryDeckRepositoryImpl] as the [MemoryDeckRepository] implementation. */
    @Binds
    @Singleton
    abstract fun bindMemoryDeckRepository(
        impl: MemoryDeckRepositoryImpl
    ): MemoryDeckRepository

    /** Binds [GeminiMemoryDeckPromptBuilder] as the [MemoryDeckPromptBuilder] implementation. */
    @Binds
    @Singleton
    abstract fun bindMemoryDeckPromptBuilder(
        impl: GeminiMemoryDeckPromptBuilder
    ): MemoryDeckPromptBuilder

    /** Providers for the active-recall deck use cases. */
    companion object {

        /** Provides [GenerateMemoryDeckUseCase]. */
        @Provides
        fun provideGenerateMemoryDeckUseCase(
            repository: MemoryDeckRepository
        ): GenerateMemoryDeckUseCase = GenerateMemoryDeckUseCase(repository)

        /** Provides [EnsureMemoryDeckUseCase]. */
        @Provides
        fun provideEnsureMemoryDeckUseCase(
            ensureTopicContent: EnsureTopicContentUseCase,
            generateMemoryDeck: GenerateMemoryDeckUseCase,
        ): EnsureMemoryDeckUseCase = EnsureMemoryDeckUseCase(ensureTopicContent, generateMemoryDeck)
    }
}
