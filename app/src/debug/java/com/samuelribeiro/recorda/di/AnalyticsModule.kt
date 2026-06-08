package com.samuelribeiro.recorda.di

import com.samuelribeiro.recorda.analytics.AnalyticsTracker
import com.samuelribeiro.recorda.analytics.LoggingAnalyticsTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    abstract fun bindAnalyticsTracker(impl: LoggingAnalyticsTracker): AnalyticsTracker
}
