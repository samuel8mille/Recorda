package com.samuelribeiro.recorda.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.samuelribeiro.recorda.BuildConfig
import com.samuelribeiro.recorda.data.prompt.GeminiFlashcardPromptBuilder
import com.samuelribeiro.recorda.data.prompt.GeminiMindMapPromptBuilder
import com.samuelribeiro.recorda.data.prompt.GeminiOralAnswerPromptBuilder
import com.samuelribeiro.recorda.data.prompt.GeminiTopicContentPromptBuilder
import com.samuelribeiro.recorda.data.repository.MindMapRepositoryImpl
import com.samuelribeiro.recorda.data.repository.OralTestRepositoryImpl
import com.samuelribeiro.recorda.data.repository.ReviewRepositoryImpl
import com.samuelribeiro.recorda.data.repository.TopicContentRepositoryImpl
import com.samuelribeiro.recorda.data.repository.TopicRepositoryImpl
import com.samuelribeiro.recorda.data.source.local.AppDatabase
import com.samuelribeiro.recorda.data.source.local.FlashcardReviewDao
import com.samuelribeiro.recorda.data.source.local.MIGRATION_7_8
import com.samuelribeiro.recorda.data.source.local.SyncCommandDao
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.domain.prompt.FlashcardPromptBuilder
import com.samuelribeiro.recorda.domain.prompt.MindMapPromptBuilder
import com.samuelribeiro.recorda.domain.prompt.OralAnswerPromptBuilder
import com.samuelribeiro.recorda.domain.prompt.TopicContentPromptBuilder
import com.samuelribeiro.recorda.data.source.remote.api.GeminiApi
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.data.source.remote.service.RetrofitGeminiService
import com.samuelribeiro.recorda.domain.repository.MindMapRepository
import com.samuelribeiro.recorda.domain.repository.OralTestRepository
import com.samuelribeiro.recorda.domain.repository.ReviewRepository
import com.samuelribeiro.recorda.domain.repository.TopicContentRepository
import com.samuelribeiro.recorda.domain.repository.TopicRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindTopicRepository(
        impl: TopicRepositoryImpl
    ): TopicRepository

    /** Binds [ReviewRepositoryImpl] as the [ReviewRepository] implementation. */
    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        impl: ReviewRepositoryImpl
    ): ReviewRepository

    /** Binds [GeminiFlashcardPromptBuilder] as the [FlashcardPromptBuilder] implementation. */
    @Binds
    @Singleton
    abstract fun bindFlashcardPromptBuilder(
        impl: GeminiFlashcardPromptBuilder
    ): FlashcardPromptBuilder

    /** Binds [OralTestRepositoryImpl] as the [OralTestRepository] implementation. */
    @Binds
    @Singleton
    abstract fun bindOralTestRepository(
        impl: OralTestRepositoryImpl
    ): OralTestRepository

    /** Binds [GeminiOralAnswerPromptBuilder] as the [OralAnswerPromptBuilder] implementation. */
    @Binds
    @Singleton
    abstract fun bindOralAnswerPromptBuilder(
        impl: GeminiOralAnswerPromptBuilder
    ): OralAnswerPromptBuilder

    /** Binds [MindMapRepositoryImpl] as the [MindMapRepository] implementation. */
    @Binds
    @Singleton
    abstract fun bindMindMapRepository(
        impl: MindMapRepositoryImpl
    ): MindMapRepository

    /** Binds [GeminiMindMapPromptBuilder] as the [MindMapPromptBuilder] implementation. */
    @Binds
    @Singleton
    abstract fun bindMindMapPromptBuilder(
        impl: GeminiMindMapPromptBuilder
    ): MindMapPromptBuilder

    /** Binds [TopicContentRepositoryImpl] as the [TopicContentRepository] implementation. */
    @Binds
    @Singleton
    abstract fun bindTopicContentRepository(
        impl: TopicContentRepositoryImpl
    ): TopicContentRepository

    /** Binds [GeminiTopicContentPromptBuilder] as the [TopicContentPromptBuilder] implementation. */
    @Binds
    @Singleton
    abstract fun bindTopicContentPromptBuilder(
        impl: GeminiTopicContentPromptBuilder
    ): TopicContentPromptBuilder

    companion object {

        @Provides
        @Singleton
        fun provideGeminiApi(retrofit: Retrofit): GeminiApi =
            retrofit.create(GeminiApi::class.java)

        @Provides
        @Singleton
        fun provideGeminiService(impl: RetrofitGeminiService): GeminiService = impl

        @Provides
        @Singleton
        @Named("baseUrl")
        fun provideBaseUrl(): String = BuildConfig.GEMINI_BASE_URL

        @Provides
        @Singleton
        @Named("debugMode")
        fun provideDebugMode(): Boolean = BuildConfig.DEBUG

        @Provides
        @Singleton
        @Named("geminiApiKey")
        fun provideGeminiApiKey(): String = BuildConfig.GEMINI_API_KEY

        @Provides
        @Singleton
        fun provideGson(): Gson = Gson()

        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "recorda.db")
                .addMigrations(MIGRATION_7_8)
                .fallbackToDestructiveMigration(true)
                .build()

        @Provides
        @Singleton
        fun provideTopicDao(database: AppDatabase): TopicDao = database.topicDao()

        /** Provides the [FlashcardReviewDao] from the [AppDatabase] singleton. */
        @Provides
        @Singleton
        fun provideFlashcardReviewDao(database: AppDatabase): FlashcardReviewDao =
            database.flashcardReviewDao()

        /** Provides the [SyncCommandDao] from the [AppDatabase] singleton. */
        @Provides
        @Singleton
        fun provideSyncCommandDao(database: AppDatabase): SyncCommandDao =
            database.syncCommandDao()
    }
}
