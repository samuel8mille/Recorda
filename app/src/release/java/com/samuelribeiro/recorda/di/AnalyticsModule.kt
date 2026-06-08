package com.samuelribeiro.recorda.di

import android.content.Context
import com.samuelribeiro.recorda.analytics.AnalyticsTracker
import com.samuelribeiro.recorda.analytics.FirebaseAnalyticsTracker
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    abstract fun bindAnalyticsTracker(impl: FirebaseAnalyticsTracker): AnalyticsTracker

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics =
            FirebaseAnalytics.getInstance(context)
    }
}
