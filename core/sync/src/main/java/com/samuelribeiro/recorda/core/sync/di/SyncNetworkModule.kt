package com.samuelribeiro.recorda.core.sync.di

import android.content.Context
import androidx.work.WorkManager
import com.samuelribeiro.recorda.core.sync.remote.SyncApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Provides the Retrofit stack for the sync backend plus the [WorkManager] used to schedule sync.
 *
 * Reuses the singleton [OkHttpClient] from `:core:network` (Chucker + logging) but points at
 * a separate base URL — `@Named("syncBaseUrl")`, supplied by `:app` from `BuildConfig` since
 * this module has no app `BuildConfig`.
 */
@Module
@InstallIn(SingletonComponent::class)
object SyncNetworkModule {

    /** Provides the app's [WorkManager] singleton used by the scheduler. */
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    /** Builds the Retrofit instance pointed at the sync backend base URL. */
    @Provides
    @Singleton
    @Named("syncRetrofit")
    fun provideSyncRetrofit(
        okHttpClient: OkHttpClient,
        @Named("syncBaseUrl") baseUrl: String,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /** Creates the [SyncApi] from the sync Retrofit instance. */
    @Provides
    @Singleton
    fun provideSyncApi(@Named("syncRetrofit") retrofit: Retrofit): SyncApi =
        retrofit.create(SyncApi::class.java)
}
