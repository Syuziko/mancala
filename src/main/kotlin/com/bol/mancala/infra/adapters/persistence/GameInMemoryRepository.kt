package com.bol.mancala.infra.adapters.persistence

import com.bol.mancala.domain.models.Game
import com.bol.mancala.domain.ports.GameRepository
import org.springframework.stereotype.Component
import java.util.UUID
import com.bol.mancala.infra.adapters.persistence.entities.Game as GameDao

@Component
internal class GameInMemoryRepository(val dataStore: MutableMap<UUID, GameDao>) : GameRepository {
    override fun create(game: Game) {
        if (dataStore.containsKey(game.gameId.toUuid())) throw GameConstraintViolationException()
        dataStore[game.gameId.toUuid()] = game.toDao()
    }

    override fun update(game: Game) {
        dataStore[game.gameId.toUuid()] = game.toDao()
    }

    override fun findById(id: Game.GameId): Game? =
        dataStore[id.toUuid()]?.toDomain()

    class GameConstraintViolationException : RuntimeException()
}


private fun Game.toDao() = GameDao(
    this.gameId.toUuid(),
    this.players.playerAt(0).name(),
    this.players.playerAt(1).name(),
    this.board.toDao(),
    this.status.name
)

private fun Game.Board.toDao() = GameDao.Board(this.rows.toDao())
private fun Game.Board.Rows.toDao() = listOf(this.row1.toDao(), this.row2.toDao())
private fun Game.Board.Rows.Row.toDao() = GameDao.Board.Row(this.pits.map { it.toDao() })
private fun Game.Board.Rows.Row.Pit.toDao() = GameDao.Board.Row.Pit(this.stones)

private fun GameDao.toDomain() = Game.of(
    Game.GameId.of(this.id.toString()),
    Game.Players.of(Game.Players.Player.of(this.firstPlayer), Game.Players.Player.of(this.secondPlayer)),
    this.board.toDomain(),
    Game.Status.valueOf(this.status)
)

private fun GameDao.Board.toDomain() = Game.Board.of(this.rows[0].toDomain(), this.rows[1].toDomain())
private fun GameDao.Board.Row.toDomain(): Game.Board.Rows.Row {
    var pits = mutableListOf<Game.Board.Rows.Row.Pit>()
    for ((index, value) in this.pits.withIndex()) {
        if (index == Game.Board.Rows.Row.MANCALA_PIT_INDEX) {
            pits.add(Game.Board.Rows.Row.MancalaPit(value.stones))
        } else {
            pits.add(Game.Board.Rows.Row.SmallPit(value.stones))
        }
    }
    return Game.Board.Rows.Row(pits)
}