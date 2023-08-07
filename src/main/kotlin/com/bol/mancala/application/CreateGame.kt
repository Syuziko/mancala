package com.bol.mancala.application

import com.bol.mancala.domain.models.Game
import com.bol.mancala.domain.ports.GameRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
internal class CreateGame(private val gameRepository: GameRepository) {
    fun start(input: Input) {

        gameRepository.create(
            Game.of(
                Game.GameId.of(input.gameId),
                Game.Players.of(Game.Players.Player.of(input.player1), Game.Players.Player.of(input.player2)),
            ),
        )
    }

    data class Input(val gameId: String, val player1: String, val player2: String)
}