package com.filmo.ui.profile

import com.filmo.service.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class RemoteDebugAuthTestClient @Inject constructor(
    private val apiService: ApiService
) : DebugAuthTestClient {
    override suspend fun runAuthTest(
        credential: DebugAuthTestCredential
    ): Result<DebugAuthTestResult> = runCatching {
        val loginBody = buildJsonObject {
            put("loginId", credential.loginId)
            put("password", credential.password)
        }.toString().toRequestBody(JsonContentType)

        val loginResponse = JsonParser
            .parseToJsonElement(apiService.login(loginBody).string())
            .jsonObject
        val accessToken = loginResponse["data"]
            ?.jsonObject
            ?.get("accessToken")
            ?.jsonPrimitive
            ?.contentOrNull
            ?: error("Missing accessToken")

        val authResponse = JsonParser
            .parseToJsonElement(apiService.authTest("Bearer $accessToken").string())
            .jsonObject
        val authData = authResponse["data"]?.jsonPrimitive?.contentOrNull
            ?: authResponse["data"]?.toString()
            ?: ""

        DebugAuthTestResult(
            loginId = credential.loginId,
            authData = authData
        )
    }

    private companion object {
        val JsonContentType = "application/json; charset=utf-8".toMediaType()
        val JsonParser = Json { ignoreUnknownKeys = true }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ProfileDebugAuthModule {
    @Provides
    fun provideDebugAuthTestClient(
        remoteDebugAuthTestClient: RemoteDebugAuthTestClient
    ): DebugAuthTestClient = remoteDebugAuthTestClient
}
