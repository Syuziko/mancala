package com.bol.mancala.application

import com.bol.mancala.domain.models.Game
import com.bol.mancala.domain.ports.GameRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.UUID

@ExtendWith(MockKExtension::class)
class CreateGameTest {

    private val gameRepository = mockk<GameRepository>()

    private val underTest = CreateGame(gameRepository)

    @Nested
    inner class PlayersTest {

        @ParameterizedTest
        @ValueSource(strings = ["", " ", "   "])
        fun `given blank player1, then throw an exception`(source: String) {
            val input = CreateGame.Input(UUID.randomUUID().toString(), source, "player2")
            shouldThrow<Game.Players.Player.BlankPlayerNameException> { underTest.start(input) }
        }

        @Test
        fun `given player1 name length more than 50, then throw an exception`() {
            val input = CreateGame.Input(UUID.randomUUID().toString(), "N".repeat(51), "player2")
            shouldThrow<Game.Players.Player.TooLongPlayerNameException> { underTest.start(input) }
        }

        @ParameterizedTest
        @ValueSource(strings = ["", " ", "   "])
        fun `given blank player2, then throw an exception`(source: String) {
            val input = CreateGame.Input(UUID.randomUUID().toString(), "player1", source)
            shouldThrow<Game.Players.Player.BlankPlayerNameException> { underTest.start(input) }
        }

        @Test
        fun `given player2 name length more than 50, then throw an exception`() {
            val input = CreateGame.Input(UUID.randomUUID().toString(), "player1", "N".repeat(51))
            shouldThrow<Game.Players.Player.TooLongPlayerNameException> { underTest.start(input) }
        }
    }

    @Nested
    inner class GameIdTests {

        @ParameterizedTest
        @ValueSource(strings = ["", " ", "   "])
        fun `given blank game id, then throw an exception`(source: String) {
            val input = CreateGame.Input(source, "player1", "player2")
            shouldThrow<Game.GameId.BlankGameIdException> { underTest.start(input) }
        }
    }

    @Test
    fun `given valid input, then persist data`() {
        val input = CreateGame.Input(UUID.randomUUID().toString(), "player1", "player2")
        val gameSlot = slot<Game>()

        justRun { gameRepository.create(capture(gameSlot)) }

        underTest.start(input)

        val result = gameSlot.captured
        result.gameId.toString() shouldBe input.gameId
        result.status shouldBe Game.Status.IN_PROGRESS
        result.players.playerAt(0).name() shouldBe input.player1
        result.players.playerAt(1).name() shouldBe input.player2
        result.board shouldNot beNull()
        result.board.rows.row1.pits.size shouldBe 7
        result.board.rows.row1.pits.count { it is Game.Board.Rows.Row.MancalaPit } shouldBe 1
        result.board.rows.row2.pits.size shouldBe 7
        result.board.rows.row2.pits.count { it is Game.Board.Rows.Row.MancalaPit } shouldBe 1

        verify(exactly = 1) { gameRepository.create(any()) }
    }
}