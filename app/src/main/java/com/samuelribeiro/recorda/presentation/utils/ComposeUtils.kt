package com.samuelribeiro.recorda.presentation.utils

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel

/**
 * Retrieves a Hilt ViewModel that requires an assisted-injection factory.
 *
 * @param VM The reified type of the [ViewModel] to be created.
 * @param FACTORY The reified type of the assisted-injection factory.
 * @param creationCallback Receives the factory and returns the ViewModel instance.
 * @return An instance of the requested ViewModel.
 */
@Composable
inline fun <reified VM : ViewModel, reified FACTORY : Any> getViewModel(
    noinline creationCallback: (FACTORY) -> VM
): VM {
    return hiltViewModel(
        creationCallback = creationCallback
    )
}
