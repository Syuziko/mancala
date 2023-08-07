package com.bol.mancala.application

import com.bol.mancala.domain.models.Game
import com.bol.mancala.domain.ports.GameRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExtendWith(MockKExtension::class)
internal class GetGameTest {

    private val gameRepository = mockk<GameRepository>()
    private val underTest = GetGame(gameRepository)

    @Test
    fun `given none existent game by id, then throw an exception`() {
        val gameId = UUID.randomUUID().toString()
        val input: GetGame.Input = GetGame.Input(gameId)

        every { gameRepository.findById(Game.GameId.of(gameId)) } returns null

        shouldThrow<GetGame.GameNotFoundException> { underTest.get(input) }
    }

    @Test
    fun `given existent game by id, then return the game`() {
        val gameId = UUID.randomUUID().toString()
        val input: GetGame.Input = GetGame.Input(gameId)

        val game = Game.of(
            Game.GameId.of(gameId), Game.Players.of(
                Game.Players.Player.of("First player"), Game.Players.Player.of("Second player")
            )
        )

        every { gameRepository.findById(Game.GameId.of(gameId)) } returns game

        val result = underTest.get(input)

        result.game.gameId shouldBe game.gameId.toString()
        result.game.players[0] shouldBe game.players.playerAt(0).name()
        result.game.players[1] shouldBe game.players.playerAt(1).name()

        result.game.board.rows[0].pits shouldContainExactly game.board.rows.row1.pits.map { it.stones }
        result.game.board.rows[1].pits shouldContainExactly game.board.rows.row2.pits.map { it.stones }

    }
}