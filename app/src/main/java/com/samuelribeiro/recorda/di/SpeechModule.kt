package com.samuelribeiro.recorda.di

import com.samuelribeiro.recorda.data.speech.AndroidSpeechToTextEngine
import com.samuelribeiro.recorda.domain.speech.SpeechToTextEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt module binding the speech-to-text engine implementation. */
@Module
@InstallIn(SingletonComponent::class)
abstract class SpeechModule {

    /** Binds [AndroidSpeechToTextEngine] as the singleton [SpeechToTextEngine]. */
    @Binds
    @Singleton
    abstract fun bindSpeechToTextEngine(impl: AndroidSpeechToTextEngine): SpeechToTextEngine
}
