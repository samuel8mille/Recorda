package com.samuelribeiro.recorda.feature.mindmap

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.samuelribeiro.recorda.presentation.navigation.MindMapSessionEntry

/**
 * Registers [MindMapScreen] into [MindMapSessionEntry] automatically at app startup,
 * before any composable is shown. This follows the same auto-init pattern used by
 * WorkManager, Firebase and other libraries.
 *
 * Runs before Application.onCreate(), so [MindMapSessionEntry.content] is always set
 * by the time the user navigates to the mind map route.
 */
internal class MindMapSessionInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        MindMapSessionEntry.content = { viewModel, onNavigateBack ->
            MindMapScreen(viewModel = viewModel, onNavigateBack = onNavigateBack)
        }
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
