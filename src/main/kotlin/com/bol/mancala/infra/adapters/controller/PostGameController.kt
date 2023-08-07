package com.bol.mancala.infra.adapters.controller

import com.bol.mancala.application.CreateGame
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/games")
internal class PostGameController(private val service: CreateGame) {

    @PostMapping
    fun create(@RequestBody request: CreateGameRequest): ResponseEntity<CreateGameResponse> {
        val gameId = UUID.randomUUID().toString()
        service.start(CreateGame.Input(gameId, request.player1, request.player2))
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateGameResponse(gameId))
    }

    internal class CreateGameRequest(val player1: String, val player2: String)
    internal class CreateGameResponse(@JsonProperty("game_id") val gameId: String)
}