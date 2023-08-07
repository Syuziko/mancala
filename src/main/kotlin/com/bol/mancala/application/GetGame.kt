package com.bol.mancala.application

import com.bol.mancala.domain.failures.ErrorCode
import com.bol.mancala.domain.failures.NotFoundException
import com.bol.mancala.domain.models.Game
import com.bol.mancala.domain.ports.GameRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
internal class GetGame(private val gameRepository: GameRepository) {
    fun get(input: Input): Output {

        val gameId = Game.GameId.of(input.gameId)

        return gameRepository.findById(gameId)?.toOutput() ?: throw GameNotFoundException(input.gameId)
    }

    data class Input(val gameId: String)

    data class Output(val game: Game) {
        class Game(
            val gameId: String,
            val players: List<String>,
            val board: Board,
        ) {
            class Board(val rows: List<Row>) {
                class Row(val pits: List<Int>)
            }
        }
    }

    private fun Game.toOutput() =
        Output(Output.Game(this.gameId.toString(), this.players.toOutput(), this.board.toOutput()))

    private fun Game.Players.toOutput() = listOf(this.playerAt(0).name(), this.playerAt(1).name())
    private fun Game.Board.toOutput() = Output.Game.Board(
        listOf(
            Output.Game.Board.Row(this.rows.row1.pits.map { it.stones }),
            Output.Game.Board.Row(this.rows.row2.pits.map { it.stones }),
        ),
    )

    class GameNotFoundException(value: String) :
        NotFoundException(ErrorCode.GAME_NOT_FOUND, "Game with id - {$value} not found.")
}