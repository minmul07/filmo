package com.filmo.service

interface AppRepository {
    suspend fun ping(): Result<String>

    suspend fun fetchItems(): Result<List<SampleItem>>

    suspend fun submitItem(request: SampleItemRequest): Result<SampleItem>
}

data class SampleItem(
    val id: String,
    val title: String,
    val description: String
)

data class SampleItemRequest(
    val title: String,
    val description: String
)
