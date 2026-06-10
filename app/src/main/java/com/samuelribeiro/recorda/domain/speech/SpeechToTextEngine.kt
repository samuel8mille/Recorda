package com.samuelribeiro.recorda.domain.speech

/** Contract for speech-to-text capture. Implementations are swappable (e.g. Android SpeechRecognizer → Whisper). */
interface SpeechToTextEngine {

    /**
     * Starts listening for speech and suspends until a transcription or error is produced.
     *
     * @return [Result.success] with the transcribed text, or [Result.failure] if recognition
     *   is unavailable, fails, or returns no speech.
     */
    suspend fun listen(): Result<String>

    /** Cancels any in-progress listening without destroying the engine. */
    fun cancel()
}
