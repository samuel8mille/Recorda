package com.samuelribeiro.recorda.di

import com.samuelribeiro.recorda.data.tts.AndroidTextToSpeechEngine
import com.samuelribeiro.recorda.domain.tts.TextToSpeechEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt module binding the TTS engine implementation. */
@Module
@InstallIn(SingletonComponent::class)
abstract class TtsModule {

    /** Binds [AndroidTextToSpeechEngine] as the singleton [TextToSpeechEngine]. */
    @Binds
    @Singleton
    abstract fun bindTextToSpeechEngine(impl: AndroidTextToSpeechEngine): TextToSpeechEngine
}
