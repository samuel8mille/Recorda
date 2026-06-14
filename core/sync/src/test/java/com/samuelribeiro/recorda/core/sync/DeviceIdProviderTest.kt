package com.samuelribeiro.recorda.core.sync

import android.content.Context
import android.content.SharedPreferences
import com.samuelribeiro.recorda.core.sync.remote.RegisterDeviceRequest
import com.samuelribeiro.recorda.core.sync.remote.RegisterDeviceResponse
import com.samuelribeiro.recorda.core.sync.remote.SyncApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceIdProviderTest {

    private val stored = mutableMapOf<String, String?>()
    private val editor: SharedPreferences.Editor = mockk(relaxed = true) {
        every { putString(any(), any()) } answers {
            stored[firstArg()] = secondArg()
            this@mockk
        }
    }
    private val prefs: SharedPreferences = mockk {
        every { getString(any(), any()) } answers { stored[firstArg()] ?: secondArg() }
        every { contains(any()) } answers { stored.containsKey(firstArg()) }
        every { edit() } returns editor
    }
    private val context: Context = mockk {
        every { getSharedPreferences(any(), any()) } returns prefs
    }
    private val api: SyncApi = mockk()

    private val provider = DeviceIdProvider(context)

    @Test
    fun `deviceId generates and persists a UUID once`() {
        val first = provider.deviceId
        val second = provider.deviceId

        assertEquals(first, second)
        assertTrue(first.isNotBlank())
        verify(atLeast = 1) { editor.putString("device_id", first) }
    }

    @Test
    fun `ensureRegistered registers on first call and caches accountId`() = runTest {
        val accountSlot = slot<String>()
        coEvery { api.registerDevice(any()) } returns RegisterDeviceResponse("account-9")
        every { editor.putString("account_id", capture(accountSlot)) } answers {
            stored["account_id"] = accountSlot.captured
            editor
        }

        val deviceId = provider.ensureRegistered(api)

        coVerify(exactly = 1) { api.registerDevice(RegisterDeviceRequest(deviceId)) }
        assertEquals("account-9", stored["account_id"])
    }

    @Test
    fun `ensureRegistered does not re-register when accountId already cached`() = runTest {
        stored["account_id"] = "account-9"

        provider.ensureRegistered(api)

        coVerify(exactly = 0) { api.registerDevice(any()) }
    }
}
