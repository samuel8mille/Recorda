package com.samuelribeiro.recorda.core.mvi

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/** Delivers UI events from the UI layer to the ViewModel via a [SharedFlow]. */
interface UiEvent<T> {
    val eventFlow: SharedFlow<T>
    suspend fun sendEvent(event: T)
}

/** Default [UiEvent] implementation backed by a [MutableSharedFlow]. */
class UiEventImpl<T> : UiEvent<T> {
    override val eventFlow = MutableSharedFlow<T>()

    override suspend fun sendEvent(event: T) {
        eventFlow.emit(event)
    }
}
