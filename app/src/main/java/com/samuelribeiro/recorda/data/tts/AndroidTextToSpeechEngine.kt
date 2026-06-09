package com.samuelribeiro.recorda.data.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import com.samuelribeiro.recorda.domain.tts.TextToSpeechEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * [TextToSpeechEngine] implementation backed by Android's built-in [TextToSpeech] API.
 *
 * Initialization is asynchronous; [speak] and [stop] are no-ops until the engine is ready.
 * Locale is set to Brazilian Portuguese (pt-BR); falls back gracefully if unavailable.
 */
class AndroidTextToSpeechEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) : TextToSpeechEngine {

    private var tts: TextToSpeech? = null
    private val ready = AtomicBoolean(false)

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("pt", "BR")
                ready.set(true)
            }
        }
    }

    override fun speak(text: String) {
        if (ready.get()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun stop() {
        if (ready.get()) tts?.stop()
    }
}
