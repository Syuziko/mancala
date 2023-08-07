package com.bol.mancala.infra.adapters.controller

import com.bol.mancala.application.PlayGame
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/games/{game_id}")
internal class PutGameController(private val service: PlayGame) {

    @PutMapping
    fun play(@PathVariable("game_id") gameId: String, @RequestBody request: PlayGameRequest): ResponseEntity<Unit> {
        service.play(
            PlayGame.Input(gameId, request.playerIndex, request.pitIndex)
        )
        return ResponseEntity.noContent().build()
    }


    data class PlayGameRequest(
        @JsonProperty("pit_index") val pitIndex: Int,
        @JsonProperty("player_index") val playerIndex: Int
    )

}