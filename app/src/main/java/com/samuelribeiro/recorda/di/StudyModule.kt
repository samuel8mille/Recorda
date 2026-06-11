package com.samuelribeiro.recorda.di

import com.samuelribeiro.recorda.data.prompt.GeminiStudyGuidePromptBuilder
import com.samuelribeiro.recorda.data.repository.StudyGuideRepositoryImpl
import com.samuelribeiro.recorda.data.source.remote.api.WikipediaApi
import com.samuelribeiro.recorda.data.source.remote.service.RetrofitWikipediaService
import com.samuelribeiro.recorda.data.source.remote.service.WikipediaImageService
import com.samuelribeiro.recorda.domain.prompt.StudyGuidePromptBuilder
import com.samuelribeiro.recorda.domain.repository.StudyGuideRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Hilt module providing the study guide feature dependencies, including the second
 * Retrofit instance pointed at the public Wikipedia API (separate module to keep
 * [DataModule] under the detekt TooManyFunctions threshold).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class StudyModule {

    /** Binds [StudyGuideRepositoryImpl] as the [StudyGuideRepository] implementation. */
    @Binds
    @Singleton
    abstract fun bindStudyGuideRepository(
        impl: StudyGuideRepositoryImpl
    ): StudyGuideRepository

    /** Binds [GeminiStudyGuidePromptBuilder] as the [StudyGuidePromptBuilder] implementation. */
    @Binds
    @Singleton
    abstract fun bindStudyGuidePromptBuilder(
        impl: GeminiStudyGuidePromptBuilder
    ): StudyGuidePromptBuilder

    /** Concrete providers for the Wikipedia Retrofit stack. */
    companion object {

        private const val WIKIPEDIA_BASE_URL = "https://pt.wikipedia.org/"

        /**
         * Provides a Retrofit instance for the Wikipedia API, reusing the shared
         * [OkHttpClient] (timeouts, Chucker and logging interceptors).
         */
        @Provides
        @Singleton
        @Named("wikipediaRetrofit")
        fun provideWikipediaRetrofit(okHttpClient: OkHttpClient): Retrofit =
            Retrofit.Builder()
                .baseUrl(WIKIPEDIA_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        /** Provides the [WikipediaApi] backed by the Wikipedia Retrofit instance. */
        @Provides
        @Singleton
        fun provideWikipediaApi(@Named("wikipediaRetrofit") retrofit: Retrofit): WikipediaApi =
            retrofit.create(WikipediaApi::class.java)

        /** Provides [RetrofitWikipediaService] as the [WikipediaImageService] implementation. */
        @Provides
        @Singleton
        fun provideWikipediaImageService(impl: RetrofitWikipediaService): WikipediaImageService = impl
    }
}
