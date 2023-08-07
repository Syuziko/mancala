package com.bol.mancala.infra.adapters.controller

import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.restassured.common.mapper.TypeRef
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Test
import java.util.UUID

internal class PutGameControllerIT : BaseControllerIT() {

    @Test
    fun `given non existent game, then return 404`() {
        val gameId = UUID.randomUUID().toString()
        val requestBody = """
           {
              "player_index": 0,
              "pit_index": 0
           }
        """.trimIndent()
        Given {
            contentType(ContentType.JSON)
            body(requestBody)
        } When {
            put("/games/$gameId/players/0")
        } Then {
            statusCode(404)
        }
    }

    @Test
    fun `given existent game, then sow stones`() {
        val gameId = createGame()
        val requestBody = """
           {
              "player_index": 0, 
              "pit_index": 0
           }
        """.trimIndent()
        Given {
            contentType(ContentType.JSON)
            body(requestBody)
        } When {
            put("/games/$gameId")
        } Then {
            statusCode(204)
        }
    }

    @Test
    fun `given existent game, then play till the end`() {
        val gameId = createGame()
        playGame(0, 0, gameId)
        playGame(1, 0, gameId)
        playGame(0, 1, gameId)
        playGame(0, 0, gameId)
        playGame(1, 1, gameId)
        playGame(2, 0, gameId)
        playGame(2, 1, gameId)
        playGame(2, 0, gameId)
        playGame(1, 1, gameId)
        playGame(0, 0, gameId)
        playGame(0, 1, gameId)
        playGame(1, 0, gameId)
        playGame(5, 1, gameId)
        playGame(3, 0, gameId)
        playGame(4, 1, gameId)
        playGame(1, 0, gameId)
        playGame(5, 1, gameId)

        val result =  playGame(0, 1, gameId)
        result shouldNot beNull()

        When {
            get("/games/$gameId")
        } Then {
            statusCode(200)
            val response: GetGameController.GameResponse =
                extract().response().`as`(object : TypeRef<GetGameController.GameResponse>() {})

            response.gameId shouldBe gameId
            response.board.rows[0].pits[6] shouldBe 39
            response.board.rows[1].pits[6] shouldBe 33

            response.board.rows[0].pits.sum() shouldBe 39
            response.board.rows[1].pits.sum() shouldBe 33
        }

    }

    private fun playGame(pit: Int, player: Int, gameId: String) {
        val requestBody = """
           {
              "player_index": $player, 
              "pit_index": $pit
           }
        """.trimIndent()
        Given {
            contentType(ContentType.JSON)
            body(requestBody)
        } When {
            put("/games/$gameId")
        } Then {
            statusCode(204)
        }
    }

    private fun createGame(): String {
        lateinit var gameId: String
        val requestBody = """
           {
              "player1": "Player 1",
              "player2": "Player 2"
           }
        """.trimIndent()
        Given {
            contentType(ContentType.JSON)
            body(requestBody)
        } When {
            post("/games")
        } Then {
            statusCode(201)
            val response: PostGameController.CreateGameResponse =
                extract().response().`as`(object : TypeRef<PostGameController.CreateGameResponse>() {})

            gameId = response.gameId
        }
        return gameId
    }
}