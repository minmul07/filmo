package com.filmo.ui.ticket

import com.filmo.service.PublicTicket
import org.junit.Assert.assertFalse
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class TicketViewPublicTicketContractTest {
    @Test
    fun publicTicketDoesNotCarryNicknameForTicketView() {
        val publicTicketFields = PublicTicket::class.java.declaredFields.map { it.name }

        assertFalse(publicTicketFields.any { it.contains("nickname", ignoreCase = true) })
    }

    @Test
    fun ticketViewLikeButtonDoesNotRenderLikeCount() {
        val source = readProjectFile("app/src/main/java/com/filmo/ui/ticket/TicketViewScreen.kt")

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
