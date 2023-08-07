package com.bol.mancala.infra.adapters.controller

import io.kotest.matchers.shouldNotBe
import io.restassured.common.mapper.TypeRef
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Test

internal class PostGameControllerIT : BaseControllerIT() {

    @Test
    fun `given valid players names, then return 201`() {
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
            response.gameId shouldNotBe null
        }
    }
}