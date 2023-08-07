package com.bol.mancala.domain.ports

import com.bol.mancala.domain.models.Game

internal interface GameRepository {
    fun create(game: Game)

    fun update(game: Game)

    fun findById(id: Game.GameId): Game?
}