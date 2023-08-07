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

internal class GetGameControllerIT : BaseControllerIT() {

    @Test
    fun `given non existent game, then return 404`() {
        val gameId = UUID.randomUUID().toString()
        When {
            get("/games/$gameId")
        } Then {
            statusCode(404)
        }
    }

    @Test
    fun `given existent game, then return non empty result`() {
        val gameId = createGame()
        When {
            get("/games/$gameId")
        } Then {
            statusCode(200)
            val response: GetGameController.GameResponse =
                extract().response().`as`(object : TypeRef<GetGameController.GameResponse>() {})

            response.gameId shouldBe gameId
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