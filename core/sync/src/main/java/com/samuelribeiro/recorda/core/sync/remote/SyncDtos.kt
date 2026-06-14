package com.samuelribeiro.recorda.core.sync.remote

/** Request body for `POST /v1/devices/register`. */
data class RegisterDeviceRequest(val deviceId: String)

/** Response of `POST /v1/devices/register`: the (stable) account this device belongs to. */
data class RegisterDeviceResponse(val accountId: String)

/** A command serialized for upload. Mirrors the local queue row sent to the backend. */
data class SyncCommandDto(
    val id: String,
    val commandType: String,
    val entityId: String,
    val payloadJson: String,
    val createdAtMillis: Long,
)

/** Request body for `POST /v1/sync/commands`: a batch of commands to upload. */
data class PushCommandsRequest(val commands: List<SyncCommandDto>)

/**
 * Response of `POST /v1/sync/commands`: the ids the backend durably accepted. The worker
 * marks exactly these as SENT, which is robust to partial acceptance.
 */
data class PushCommandsResponse(val acceptedIds: List<String>)
