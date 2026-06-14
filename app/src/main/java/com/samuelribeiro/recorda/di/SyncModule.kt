package com.samuelribeiro.recorda.di

import com.samuelribeiro.recorda.BuildConfig
import com.samuelribeiro.recorda.core.sync.SyncQueue
import com.samuelribeiro.recorda.data.sync.RoomSyncQueue
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Wires the `:core:sync` infrastructure into the app: binds the Room-backed [SyncQueue] and
 * supplies the sync backend base URL from [BuildConfig] (which `:core:sync` cannot read).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    /** Binds [RoomSyncQueue] as the [SyncQueue] consumed by the sync worker. */
    @Binds
    @Singleton
    abstract fun bindSyncQueue(impl: RoomSyncQueue): SyncQueue

    /** Provides values `:core:sync` cannot read on its own (the app's `BuildConfig`). */
    companion object {

        /** The sync backend base URL, read from the app's `BuildConfig`. */
        @Provides
        @Singleton
        @Named("syncBaseUrl")
        fun provideSyncBaseUrl(): String = BuildConfig.SYNC_BASE_URL
    }
}
