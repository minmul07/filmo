package com.filmo.ui.collection

import org.junit.Assert.assertFalse
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class CollectionDetailTopBarContractTest {
    @Test
    fun detailTopBarDoesNotExposeDeleteActionState() {
        val topBarMethods = Class.forName("com.filmo.ui.collection.CollectionComponentsKt")
            .declaredMethods
            .filter { it.name == "CollectionDetailTopBar" }

        assertFalse(
            topBarMethods.any { method ->
                method.parameterTypes.any { parameterType ->
                    parameterType == Boolean::class.javaPrimitiveType
                }
            }
        )
    }

    @Test
    fun collectionLikeButtonDoesNotRenderLikeCount() {
        val source = readProjectFile(
            "app/src/main/java/com/filmo/ui/collection/CollectionTicketComponents.kt"
        )

        assertFalse(source.contains("likeCount = ticket.likeCount"))
        assertFalse(source.contains("likeCount.toString()"))
    }

    private fun readProjectFile(relativePath: String): String {
        val userDir = Paths.get(System.getProperty("user.dir"))
        val candidates = listOf(
            userDir.resolve(relativePath),
            userDir.resolve("..").resolve(relativePath),
            userDir.resolve("..").resolve("..").resolve(relativePath)
        ).map(Path::normalize)

        val path = candidates.first(Files::exists)
        return String(Files.readAllBytes(path))
    }
}
