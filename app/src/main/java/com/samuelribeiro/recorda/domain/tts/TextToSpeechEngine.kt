package com.samuelribeiro.recorda.domain.tts

/** Contract for text-to-speech playback. Implementations are swappable (e.g. Android TTS → Whisper). */
interface TextToSpeechEngine {

    /** Speaks [text], interrupting any speech currently in progress. */
    fun speak(text: String)

    /** Stops any speech currently in progress without destroying the engine. */
    fun stop()
}
