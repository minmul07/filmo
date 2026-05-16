package com.filmo.service

import javax.inject.Inject

class RemoteAppRepository @Inject constructor(
    private val apiService: ApiService
) : AppRepository {
    override suspend fun ping(): Result<String> = runCatching {
        apiService.ping().string()
    }

    override suspend fun fetchItems(): Result<List<SampleItem>> = runCatching {
        val responseText = apiService.fetchItems().string()
        listOf(
            SampleItem(
                id = "remote-placeholder",
                title = "Remote response",
                description = responseText
            )
        )
    }

    override suspend fun submitItem(request: SampleItemRequest): Result<SampleItem> = runCatching {
        val responseText = apiService.submitItem(
            title = request.title,
            description = request.description
        ).string()
        SampleItem(
            id = "remote-created-placeholder",
            title = request.title,
            description = responseText.ifBlank { request.description }
        )
    }
}
