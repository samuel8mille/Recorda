package com.samuelribeiro.recorda.data.speech

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.samuelribeiro.recorda.domain.speech.SpeechToTextEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * [SpeechToTextEngine] implementation backed by Android's built-in [SpeechRecognizer] API.
 *
 * [SpeechRecognizer] must be created and used on the main thread, so [listen] dispatches to
 * [Dispatchers.Main]. Locale is set to Brazilian Portuguese (pt-BR). If recognition is
 * unavailable on the device (e.g. no Google app, common on CI emulators), [listen] fails
 * immediately instead of hanging.
 */
class AndroidSpeechToTextEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechToTextEngine {

    private var recognizer: SpeechRecognizer? = null

    override suspend fun listen(): Result<String> = withContext(Dispatchers.Main) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return@withContext Result.failure(IllegalStateException("Speech recognition unavailable"))
        }

        suspendCancellableCoroutine { continuation ->
            val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            recognizer = speechRecognizer

            speechRecognizer.setRecognitionListener(
                object : RecognitionListener {
                    override fun onResults(results: Bundle?) {
                        val text = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                        if (continuation.isActive) {
                            continuation.resume(
                                text?.let { Result.success(it) }
                                    ?: Result.failure(IllegalStateException("No speech recognized")),
                            )
                        }
                        speechRecognizer.destroy()
                    }

                    override fun onError(error: Int) {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(IllegalStateException("Speech recognition error $error")))
                        }
                        speechRecognizer.destroy()
                    }

                    override fun onReadyForSpeech(params: Bundle?) = Unit
                    override fun onBeginningOfSpeech() = Unit
                    override fun onRmsChanged(rmsdB: Float) = Unit
                    override fun onBufferReceived(buffer: ByteArray?) = Unit
                    override fun onEndOfSpeech() = Unit
                    override fun onPartialResults(partialResults: Bundle?) = Unit
                    override fun onEvent(eventType: Int, params: Bundle?) = Unit
                },
            )

            val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("pt", "BR").toString())
            }
            speechRecognizer.startListening(intent)

            continuation.invokeOnCancellation {
                speechRecognizer.cancel()
                speechRecognizer.destroy()
            }
        }
    }

    override fun cancel() {
        recognizer?.cancel()
    }
}
