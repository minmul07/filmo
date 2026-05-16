package minmul.androidtemplate.service

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class MockAppRepositoryTest {
    @Test
    fun pingReturnsSuccessAfterMockDelay() = runBlocking {
        val repository = MockAppRepository()

        val startedAt = System.currentTimeMillis()
        val result = repository.ping()
        val elapsedMillis = System.currentTimeMillis() - startedAt

        assertTrue(result.isSuccess)
        assertTrue(elapsedMillis >= 250)
    }

    @Test
    fun fetchItemsReturnsTemplateItemsAfterMockDelay() = runBlocking {
        val repository = MockAppRepository()

        val startedAt = System.currentTimeMillis()
        val result = repository.fetchItems()
        val elapsedMillis = System.currentTimeMillis() - startedAt

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isNotEmpty())
        assertTrue(elapsedMillis >= 250)
    }

    @Test
    fun submitItemReturnsCreatedItemAfterMockDelay() = runBlocking {
        val repository = MockAppRepository()
        val request = SampleItemRequest(
            title = "Mock title",
            description = "Mock description"
        )

        val startedAt = System.currentTimeMillis()
        val result = repository.submitItem(request)
        val elapsedMillis = System.currentTimeMillis() - startedAt

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().title == request.title)
        assertTrue(elapsedMillis >= 250)
    }
}
