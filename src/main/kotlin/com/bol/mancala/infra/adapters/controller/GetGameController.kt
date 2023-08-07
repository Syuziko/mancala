package com.bol.mancala.infra.adapters.controller

import com.bol.mancala.application.GetGame
import com.bol.mancala.application.PlayGame
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("games/{game_id}")
internal class GetGameController(private val service: GetGame) {

    @GetMapping
    fun get(@PathVariable("game_id") gameId: String): ResponseEntity<GameResponse> {

        return ResponseEntity.ok(service.get(GetGame.Input(gameId)).toResponse())
    }

    class GameResponse(val gameId: String, val players: List<String>, val board: Board) {
        class Board(val rows: List<Row>) {
            class Row(val pits: List<Int>)
        }
    }

    private fun GetGame.Output.toResponse() =
        GameResponse(this.game.gameId, this.game.players, this.game.board.toResponse())

    private fun GetGame.Output.Game.Board.toResponse() = GameResponse.Board(
        listOf(
            GameResponse.Board.Row(this.rows[0].pits),
            GameResponse.Board.Row(this.rows[1].pits),
        ),
    )
}