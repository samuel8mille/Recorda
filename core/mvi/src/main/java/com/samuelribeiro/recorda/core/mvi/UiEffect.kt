package com.samuelribeiro.recorda.core.mvi

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/** Emits one-shot side effects from the ViewModel to the UI layer via a [SharedFlow]. */
interface UiEffect<T> {
    val effectFlow: SharedFlow<T>
    suspend fun sendEffect(effect: T)
}

/** Default [UiEffect] implementation backed by a [MutableSharedFlow]. */
class UiEffectImpl<T> : UiEffect<T> {
    override val effectFlow = MutableSharedFlow<T>(extraBufferCapacity = 1)

    override suspend fun sendEffect(effect: T) {
        effectFlow.emit(effect)
    }
}
