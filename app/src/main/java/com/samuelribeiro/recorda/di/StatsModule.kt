package com.samuelribeiro.recorda.di

import com.samuelribeiro.recorda.data.repository.StatsRepositoryImpl
import com.samuelribeiro.recorda.data.source.local.AppDatabase
import com.samuelribeiro.recorda.data.source.local.ReviewLogDao
import com.samuelribeiro.recorda.domain.repository.StatsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing the retention statistics dependencies (separate module to
 * keep [DataModule] under the detekt TooManyFunctions threshold).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class StatsModule {

    /** Binds [StatsRepositoryImpl] as the [StatsRepository] implementation. */
    @Binds
    @Singleton
    abstract fun bindStatsRepository(
        impl: StatsRepositoryImpl
    ): StatsRepository

    /** Concrete providers of the statistics persistence. */
    companion object {

        /** Provides the [ReviewLogDao] from the [AppDatabase] singleton. */
        @Provides
        @Singleton
        fun provideReviewLogDao(database: AppDatabase): ReviewLogDao = database.reviewLogDao()
    }
}
