package com.samuelribeiro.recorda.core.sync

import android.content.Context
import com.samuelribeiro.recorda.core.sync.remote.RegisterDeviceRequest
import com.samuelribeiro.recorda.core.sync.remote.SyncApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns this installation's stable device identity for sync.
 *
 * The device id is generated once and persisted in [SharedPreferences]; on first sync the
 * device is registered with the backend and the returned account id is cached. Without login
 * yet, one device maps to one anonymous account — multi-device comes in a later phase.
 */
@Singleton
class DeviceIdProvider @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** The stable device id, generated and persisted on first access. */
    val deviceId: String
        get() = prefs.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_DEVICE_ID, it).apply()
        }

    /**
     * Ensures the device is registered with the backend exactly once, caching the account id.
     * Returns the device id for use as the `X-Device-Id` header.
     */
    suspend fun ensureRegistered(api: SyncApi): String {
        val id = deviceId
        if (!prefs.contains(KEY_ACCOUNT_ID)) {
            val response = api.registerDevice(RegisterDeviceRequest(id))
            prefs.edit().putString(KEY_ACCOUNT_ID, response.accountId).apply()
        }
        return id
    }

    private companion object {
        const val PREFS_NAME = "recorda_sync"
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_ACCOUNT_ID = "account_id"
    }
}
