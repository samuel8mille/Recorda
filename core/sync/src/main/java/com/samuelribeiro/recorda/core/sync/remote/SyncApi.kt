package com.samuelribeiro.recorda.core.sync.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/** Retrofit contract for the offline-first sync backend (upload one-way for now). */
interface SyncApi {

    /** Registers this device (idempotent) and returns its account id. */
    @POST("v1/devices/register")
    suspend fun registerDevice(@Body body: RegisterDeviceRequest): RegisterDeviceResponse

    /** Uploads a batch of commands; returns the ids the backend durably accepted. */
    @POST("v1/sync/commands")
    suspend fun pushCommands(
        @Header("X-Device-Id") deviceId: String,
        @Body body: PushCommandsRequest,
    ): PushCommandsResponse
}
