package com.bol.mancala.application

import com.bol.mancala.domain.service.GameService
import com.bol.mancala.domain.models.Game
import com.bol.mancala.domain.ports.GameRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.UUID

@ExtendWith(MockKExtension::class)
class PlayGameTest {

    private val gameRepository = mockk<GameRepository>()
    private val sowService = GameService()

    private val underTest = PlayGame(sowService, gameRepository)

    @Test
    fun `given game does not exist by the provided id, then throw an exception`() {
        val input = PlayGame.Input(UUID.randomUUID().toString(), 0, 0)

        every { gameRepository.findById(Game.GameId.of(input.gameId)) } returns null

        shouldThrow<PlayGame.GameNotFoundException> { underTest.play(input) }
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 3, 10])
    fun `given playerIndex is out of range of players size, then throw an exception`(source: Int) {
        val input = PlayGame.Input(UUID.randomUUID().toString(), source, 0)

        every { gameRepository.findById(Game.GameId.of(input.gameId)) } returns Game.of(
            Game.GameId.of(input.gameId),
            Game.Players.of(
                Game.Players.Player.of("Player1"),
                Game.Players.Player.of("Player2"),
            ),
        )

        shouldThrow<Game.Players.PlayerIndexOutOfBoundsException> { underTest.play(input) }
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 7, 10])
    fun `given pitId is out of range of pits size, then throw an exception`(source: Int) {
        val input = PlayGame.Input(UUID.randomUUID().toString(), 0, source)

        every { gameRepository.findById(Game.GameId.of(input.gameId)) } returns Game.of(
            Game.GameId.of(input.gameId),
            Game.Players.of(
                Game.Players.Player.of("Player1"),
                Game.Players.Player.of("Player2"),
            ),
        )

        shouldThrow<Game.Board.Rows.PitIdOutOfBoundsException> { underTest.play(input) }
    }

    @Test
    fun `given pit is empty, then throw an exception`() {
        val input = PlayGame.Input(UUID.randomUUID().toString(), 0, 0)
        val pits = listOf(
            Game.Board.Rows.Row.SmallPit(0),
            Game.Board.Rows.Row.SmallPit(6),
            Game.Board.Rows.Row.SmallPit(3),
            Game.Board.Rows.Row.SmallPit(7),
            Game.Board.Rows.Row.SmallPit(6),
            Game.Board.Rows.Row.SmallPit(3),
            Game.Board.Rows.Row.MancalaPit(9),
        )
        val game = Game.of(
            Game.GameId.of(input.gameId),
            Game.Players.of(
                Game.Players.Player.of("Player1"),
                Game.Players.Player.of("Player2"),
            ),
            Game.Board.of(Game.Board.Rows.Row(pits), Game.Board.Rows.Row(pits)),
        )
        every { gameRepository.findById(Game.GameId.of(input.gameId)) } returns game

        shouldThrow<GameService.EmptyPitException> { underTest.play(input) }
    }

    @Test
    fun `given mancala pit, then throw an exception`() {
        val input = PlayGame.Input(UUID.randomUUID().toString(), 0, 6)
        val pits = listOf(
            Game.Board.Rows.Row.SmallPit(0),
            Game.Board.Rows.Row.SmallPit(6),
            Game.Board.Rows.Row.SmallPit(3),
            Game.Board.Rows.Row.SmallPit(7),
            Game.Board.Rows.Row.SmallPit(6),
            Game.Board.Rows.Row.SmallPit(3),
            Game.Board.Rows.Row.MancalaPit(9),
        )
        every { gameRepository.findById(Game.GameId.of(input.gameId)) } returns Game.of(
            Game.GameId.of(input.gameId),
            Game.Players.of(
                Game.Players.Player.of("Player1"),
                Game.Players.Player.of("Player2"),
            ),
            Game.Board.of(Game.Board.Rows.Row(pits), Game.Board.Rows.Row(pits)),
        )

        shouldThrow<GameService.UnsupportedPitForSowOperationException> { underTest.play(input) }
    }

    @Nested
    inner class EmptyPitTests {

        @Test
        fun `given final stone ends up in own empty pit, then collect opponents pit stones`() {
            val input = PlayGame.Input(UUID.randomUUID().toString(), 0, 5)
            val pitsRow1 = listOf(
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.MancalaPit(2),
            )

            val pitsRow2 = listOf(
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(9),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.MancalaPit(1),
            )
            every { gameRepository.findById(Game.GameId.of(input.gameId)) } returns Game.of(
                Game.GameId.of(input.gameId),
                Game.Players.of(
                    Game.Players.Player.of("Player1"),
                    Game.Players.Player.of("Player2"),
                ),
                Game.Board.of(Game.Board.Rows.Row(pitsRow1), Game.Board.Rows.Row(pitsRow2)),
            )
            val gameSlot = slot<Game>()
            justRun { gameRepository.update(capture(gameSlot)) }

            underTest.play(input)

            val result = gameSlot.captured
            result.gameId.toString() shouldBe input.gameId
            result.board.rows.row1.run {
                pits[0].stones shouldBe 0
                pits[1].stones shouldBe 0
                pits[2].stones shouldBe 8
                pits[3].stones shouldBe 8
                pits[4].stones shouldBe 8
                pits[5].stones shouldBe 0
                pits[6].stones shouldBe 12
            }

            result.board.rows.row2.run {
                pits[0].stones shouldBe 1
                pits[1].stones shouldBe 10
                pits[2].stones shouldBe 8
                pits[3].stones shouldBe 8
                pits[4].stones shouldBe 8
                pits[5].stones shouldBe 0
                pits[6].stones shouldBe 1
            }
        }

        @Test
        fun `given second player final stone ends up in own empty pit, then collect opponents pit stones`() {
            val input = PlayGame.Input(UUID.randomUUID().toString(), 1, 5)
            val pitsRow1 = listOf(
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(9),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.MancalaPit(1),
            )

            val pitsRow2 = listOf(
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.MancalaPit(2),
            )
            every { gameRepository.findById(Game.GameId.of(input.gameId)) } returns Game.of(
                Game.GameId.of(input.gameId),
                Game.Players.of(
                    Game.Players.Player.of("Player1"),
                    Game.Players.Player.of("Player2"),
                ),
                Game.Board.of(Game.Board.Rows.Row(pitsRow1), Game.Board.Rows.Row(pitsRow2)),
            )
            val gameSlot = slot<Game>()
            justRun { gameRepository.update(capture(gameSlot)) }

            underTest.play(input)

            val result = gameSlot.captured
            result.gameId.toString() shouldBe input.gameId

            result.board.rows.row1.run {
                pits[0].stones shouldBe 1
                pits[1].stones shouldBe 10
                pits[2].stones shouldBe 8
                pits[3].stones shouldBe 8
                pits[4].stones shouldBe 8
                pits[5].stones shouldBe 0
                pits[6].stones shouldBe 1
            }

            result.board.rows.row2.run {
                pits[0].stones shouldBe 0
                pits[1].stones shouldBe 0
                pits[2].stones shouldBe 8
                pits[3].stones shouldBe 8
                pits[4].stones shouldBe 8
                pits[5].stones shouldBe 0
                pits[6].stones shouldBe 12
            }
        }
    }

    @Nested
    inner class FinalStoneInOpponentsPitTests {

        @Test
        fun `given first player does not end up in own empty pit, then just increment pits stones count`() {
            val input = PlayGame.Input(UUID.randomUUID().toString(), 0, 0)
            val pitsRow1 = listOf(
                Game.Board.Rows.Row.SmallPit(1),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.MancalaPit(1),
            )

            val pitsRow2 = listOf(
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.MancalaPit(2),
            )

            every { gameRepository.findById(Game.GameId.of(input.gameId)) } returns Game.of(
                Game.GameId.of(input.gameId),
                Game.Players.of(
                    Game.Players.Player.of("Player1"),
                    Game.Players.Player.of("Player2"),
                ),
                Game.Board.of(Game.Board.Rows.Row(pitsRow1), Game.Board.Rows.Row(pitsRow2)),
            )
            val gameSlot = slot<Game>()
            justRun { gameRepository.update(capture(gameSlot)) }

            underTest.play(input)

            val result = gameSlot.captured
            result.gameId.toString() shouldBe input.gameId

            result.board.rows.row1.run {
                pits[0].stones shouldBe 0
                pits[1].stones shouldBe 9
                pits[2].stones shouldBe 7
                pits[3].stones shouldBe 7
                pits[4].stones shouldBe 7
                pits[5].stones shouldBe 7
                pits[6].stones shouldBe 1
            }

            result.board.rows.row2.run {
                pits[0].stones shouldBe 0
                pits[1].stones shouldBe 0
                pits[2].stones shouldBe 8
                pits[3].stones shouldBe 8
                pits[4].stones shouldBe 8
                pits[5].stones shouldBe 8
                pits[6].stones shouldBe 2
            }
        }

        @Test
        fun `given second player does not end up in own empty pit, then just increment pits stones count`() {
            val input = PlayGame.Input(UUID.randomUUID().toString(), 1, 0)
            val pitsRow1 = listOf(
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.MancalaPit(2),
            )

            val pitsRow2 = listOf(
                Game.Board.Rows.Row.SmallPit(1),
                Game.Board.Rows.Row.SmallPit(8),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.SmallPit(7),
                Game.Board.Rows.Row.MancalaPit(1),
            )
            every { gameRepository.findById(Game.GameId.of(input.gameId)) } returns Game.of(
                Game.GameId.of(input.gameId),
                Game.Players.of(
                    Game.Players.Player.of("Player1"),
                    Game.Players.Player.of("Player2"),
                ),
                Game.Board.of(Game.Board.Rows.Row(pitsRow1), Game.Board.Rows.Row(pitsRow2)),
            )
            val gameSlot = slot<Game>()
            justRun { gameRepository.update(capture(gameSlot)) }

            underTest.play(input)

            val result = gameSlot.captured
            result.gameId.toString() shouldBe input.gameId
            result.board.rows.row1.run {
                pits[0].stones shouldBe 0
                pits[1].stones shouldBe 0
                pits[2].stones shouldBe 8
                pits[3].stones shouldBe 8
                pits[4].stones shouldBe 8
                pits[5].stones shouldBe 8
                pits[6].stones shouldBe 2
            }

            result.board.rows.row2.run {
                pits[0].stones shouldBe 0
                pits[1].stones shouldBe 9
                pits[2].stones shouldBe 7
                pits[3].stones shouldBe 7
                pits[4].stones shouldBe 7
                pits[5].stones shouldBe 7
                pits[6].stones shouldBe 1
            }
        }
    }

    @Nested
    inner class EndOfGameTests {

        @Test
        fun `given sow ends up with empty second player row, then end of game`() {
            val input = PlayGame.Input(UUID.randomUUID().toString(), 1, 5)
            val pitsRow1 = listOf(
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(2),
                Game.Board.Rows.Row.SmallPit(4),
                Game.Board.Rows.Row.SmallPit(3),
                Game.Board.Rows.Row.SmallPit(16),
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.MancalaPit(35),
            )

            val pitsRow2 = listOf(
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(1),
                Game.Board.Rows.Row.MancalaPit(11),
            )
            every { gameRepository.findById(Game.GameId.of(input.gameId)) } returns Game.of(
                Game.GameId.of(input.gameId),
                Game.Players.of(
                    Game.Players.Player.of("Player1"),
                    Game.Players.Player.of("Player2"),
                ),
                Game.Board.of(Game.Board.Rows.Row(pitsRow1), Game.Board.Rows.Row(pitsRow2)),
            )
            val gameSlot = slot<Game>()
            justRun { gameRepository.update(capture(gameSlot)) }

            underTest.play(input)

            val result = gameSlot.captured
            result.gameId.toString() shouldBe input.gameId
            result.status shouldBe Game.Status.ENDED

            result.board.rows.row1.run {
                pits[0].stones shouldBe 0
                pits[1].stones shouldBe 0
                pits[2].stones shouldBe 0
                pits[3].stones shouldBe 0
                pits[4].stones shouldBe 0
                pits[5].stones shouldBe 0
                pits[6].stones shouldBe 60
            }

            result.board.rows.row2.run {
                pits[0].stones shouldBe 0
                pits[1].stones shouldBe 0
                pits[2].stones shouldBe 0
                pits[3].stones shouldBe 0
                pits[4].stones shouldBe 0
                pits[5].stones shouldBe 0
                pits[6].stones shouldBe 12
            }
        }

        @Test
        fun `given sow ends up with empty first player row, then end of game`() {
            val input = PlayGame.Input(UUID.randomUUID().toString(), 0, 5)
            val pitsRow1 = listOf(
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(1),
                Game.Board.Rows.Row.MancalaPit(11),
            )
            val pitsRow2 = listOf(
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.SmallPit(2),
                Game.Board.Rows.Row.SmallPit(4),
                Game.Board.Rows.Row.SmallPit(3),
                Game.Board.Rows.Row.SmallPit(16),
                Game.Board.Rows.Row.SmallPit(0),
                Game.Board.Rows.Row.MancalaPit(35),
            )
            every { gameRepository.findById(Game.GameId.of(input.gameId)) } returns Game.of(
                Game.GameId.of(input.gameId),
                Game.Players.of(
                    Game.Players.Player.of("Player1"),
                    Game.Players.Player.of("Player2"),
                ),
                Game.Board.of(Game.Board.Rows.Row(pitsRow1), Game.Board.Rows.Row(pitsRow2)),
            )
            val gameSlot = slot<Game>()
            justRun { gameRepository.update(capture(gameSlot)) }

            underTest.play(input)

            val result = gameSlot.captured
            result.gameId.toString() shouldBe input.gameId
            result.status shouldBe Game.Status.ENDED

            result.board.rows.row1.run {
                pits[0].stones shouldBe 0
                pits[1].stones shouldBe 0
                pits[2].stones shouldBe 0
                pits[3].stones shouldBe 0
                pits[4].stones shouldBe 0
                pits[5].stones shouldBe 0
                pits[6].stones shouldBe 12
            }

            result.board.rows.row2.run {
                pits[0].stones shouldBe 0
                pits[1].stones shouldBe 0
                pits[2].stones shouldBe 0
                pits[3].stones shouldBe 0
                pits[4].stones shouldBe 0
                pits[5].stones shouldBe 0
                pits[6].stones shouldBe 60
            }
        }
    }
}