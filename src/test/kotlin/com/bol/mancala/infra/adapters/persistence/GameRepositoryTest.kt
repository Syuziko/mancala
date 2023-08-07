package com.bol.mancala.infra.adapters.persistence

import com.bol.mancala.domain.models.Game
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import java.util.UUID
import com.bol.mancala.infra.adapters.persistence.entities.Game as GameDao

@Import(GameInMemoryRepository::class)
class GameRepositoryTest {

    private val dataStore = mutableMapOf<UUID, GameDao>()
    private val gameInMemoryRepository = GameInMemoryRepository(dataStore)

    @BeforeEach
    fun setup() {
        dataStore.clear()
    }

    @Nested
    inner class CreateGameTests {

        @Test
        fun `given game create request, then persist data into datastore`() {
            val gameId = Game.GameId.of(UUID.randomUUID().toString())
            val game = Game.of(
                gameId,
                Game.Players.of(Game.Players.Player.of("player1"), Game.Players.Player.of("player2")),
            )

            gameInMemoryRepository.create(game)

            val result = dataStore[gameId.toUuid()]

            result shouldNot beNull()
            result?.run {
                id shouldBe gameId.toUuid()
                status shouldBe Game.Status.IN_PROGRESS.name
                firstPlayer shouldBe game.players.playerAt(0).name()
                secondPlayer shouldBe game.players.playerAt(1).name()
                board shouldNot beNull()
                board.rows shouldNot beEmpty()
                board.rows.size shouldBe 2
                board.rows[0].run {
                    pits.size shouldBe 7
                    pits[0].stones shouldBe 6
                    pits[1].stones shouldBe 6
                    pits[2].stones shouldBe 6
                    pits[3].stones shouldBe 6
                    pits[4].stones shouldBe 6
                    pits[5].stones shouldBe 6
                    pits[6].stones shouldBe 0
                }
                board.rows[1].run {
                    pits.size shouldBe 7
                    pits[0].stones shouldBe 6
                    pits[1].stones shouldBe 6
                    pits[2].stones shouldBe 6
                    pits[3].stones shouldBe 6
                    pits[4].stones shouldBe 6
                    pits[5].stones shouldBe 6
                    pits[6].stones shouldBe 0
                }
            }
        }
    }

    @Nested
    inner class FindGameByIdTests {

        @Test
        fun `given game does not exist by id, then return null`() {
            val gameId = Game.GameId.of(UUID.randomUUID().toString())
            val game = Game.of(
                Game.GameId.of(UUID.randomUUID().toString()),
                Game.Players.of(Game.Players.Player.of("player1"), Game.Players.Player.of("player2")),
            )

            gameInMemoryRepository.create(game)

            val result = gameInMemoryRepository.findById(gameId)

            result should beNull()
        }

        @Test
        fun `given existent game by id, then return game`() {
            val gameId = Game.GameId.of(UUID.randomUUID().toString())
            val game = Game.of(
                gameId,
                Game.Players.of(Game.Players.Player.of("player1"), Game.Players.Player.of("player2")),
            )

            gameInMemoryRepository.create(game)

            val result = gameInMemoryRepository.findById(gameId)

            result shouldNot beNull()

            result?.run {
                gameId shouldBe gameId
                players shouldBe game.players
                board shouldBe game.board
            }
        }
    }

    @Nested
    inner class UpdateGameTests {

        @Test
        fun `given stones sowed, then update the game in data store`() {

            val gameId = Game.GameId.of(UUID.randomUUID().toString())
            val game = Game.of(
                gameId,
                Game.Players.of(Game.Players.Player.of("player1"), Game.Players.Player.of("player2")),
            )

            gameInMemoryRepository.create(game)

            game.endGame()

            gameInMemoryRepository.update(game)

            val result = dataStore[gameId.toUuid()]

            result shouldNot beNull()
            result?.run {
                id shouldBe gameId.toUuid()
                status shouldBe Game.Status.ENDED.name
                firstPlayer shouldBe game.players.playerAt(0).name()
                secondPlayer shouldBe game.players.playerAt(1).name()
                board shouldNot beNull()
                board.rows shouldNot beEmpty()
                board.rows.size shouldBe 2
                board.rows[0].run {
                    pits[0].stones shouldBe 0
                    pits[1].stones shouldBe 0
                    pits[2].stones shouldBe 0
                    pits[3].stones shouldBe 0
                    pits[4].stones shouldBe 0
                    pits[5].stones shouldBe 0
                    pits[6].stones shouldBe 36
                }
                board.rows[1].run {
                    pits[0].stones shouldBe 0
                    pits[1].stones shouldBe 0
                    pits[2].stones shouldBe 0
                    pits[3].stones shouldBe 0
                    pits[4].stones shouldBe 0
                    pits[5].stones shouldBe 0
                    pits[6].stones shouldBe 36
                }
            }

        }
    }
}