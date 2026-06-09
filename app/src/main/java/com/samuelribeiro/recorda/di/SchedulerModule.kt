package com.samuelribeiro.recorda.di

import com.samuelribeiro.recorda.domain.scheduler.ReviewScheduler
import com.samuelribeiro.recorda.domain.scheduler.Sm2ReviewScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt module that binds spaced-repetition scheduling implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulerModule {

    /** Binds [Sm2ReviewScheduler] as the active [ReviewScheduler] implementation. */
    @Binds
    @Singleton
    abstract fun bindReviewScheduler(impl: Sm2ReviewScheduler): ReviewScheduler
}
