package com.samuelribeiro.recorda.di

import com.samuelribeiro.recorda.logging.CrashReporter
import com.samuelribeiro.recorda.logging.LoggingCrashReporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CrashReporterModule {

    @Binds
    abstract fun bindCrashReporter(impl: LoggingCrashReporter): CrashReporter
}
