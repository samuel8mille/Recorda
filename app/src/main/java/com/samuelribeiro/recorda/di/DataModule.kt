package com.samuelribeiro.recorda.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.google.gson.Gson
import com.samuelribeiro.recorda.BuildConfig
import com.samuelribeiro.recorda.data.repository.TopicRepositoryImpl
import com.samuelribeiro.recorda.data.source.local.AppDatabase
import com.samuelribeiro.recorda.data.source.local.TopicDao
import com.samuelribeiro.recorda.data.source.remote.api.GeminiApi
import com.samuelribeiro.recorda.data.source.remote.service.GeminiService
import com.samuelribeiro.recorda.data.source.remote.service.RetrofitGeminiService
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
                .fallbackToDestructiveMigration(true)
                .build()

        @Provides
        @Singleton
        fun provideTopicDao(database: AppDatabase): TopicDao = database.topicDao()

        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
            WorkManager.getInstance(context)
    }
}
