package com.bol.mancala.application

import com.bol.mancala.domain.service.GameService
import com.bol.mancala.domain.failures.ErrorCode
import com.bol.mancala.domain.failures.NotFoundException
import com.bol.mancala.domain.models.Game
import com.bol.mancala.domain.ports.GameRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
internal class PlayGame(
    private val gameService: GameService,
    private val gameRepository: GameRepository,
) {
    fun play(input: Input) {
        val game = gameRepository.findById(Game.GameId.of(input.gameId)) ?: throw GameNotFoundException(input.gameId)
        gameService.play(game, input.playerIndex, input.pitId)

        gameRepository.update(game)
    }

    class Input(val gameId: String, val playerIndex: Int, val pitId: Int)

    class GameNotFoundException(value: String) :
        NotFoundException(ErrorCode.GAME_NOT_FOUND, "Game with id - {$value} not found.")
}