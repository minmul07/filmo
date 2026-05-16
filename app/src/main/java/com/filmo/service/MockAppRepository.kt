package com.filmo.service

import kotlinx.coroutines.delay
import javax.inject.Inject

class MockAppRepository @Inject constructor() : AppRepository {
    override suspend fun ping(): Result<String> = withMockDelay {
        "mock-pong"
    }

    override suspend fun fetchItems(): Result<List<SampleItem>> = withMockDelay {
        listOf(
            SampleItem(
                id = "mock-1",
                title = "Mock item",
                description = "Template item from MockAppRepository"
            ),
            SampleItem(
                id = "mock-2",
                title = "Second mock item",
                description = "Use this data while the backend is not ready"
            )
        )
    }

    override suspend fun submitItem(request: SampleItemRequest): Result<SampleItem> = withMockDelay {
        SampleItem(
            id = "mock-created",
            title = request.title,
            description = request.description
        )
    }

    private suspend fun <T> withMockDelay(block: () -> T): Result<T> = runCatching {
        delay(MOCK_DELAY_MILLIS)
        block()
    }

    private companion object {
        const val MOCK_DELAY_MILLIS = 250L
    }
}
