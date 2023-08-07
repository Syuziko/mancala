package com.bol.mancala.infra.adapters.controller

import com.bol.mancala.MancalaApplication
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("it")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [MancalaApplication::class],
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class BaseControllerIT {
    @LocalServerPort
    private var port: Int = 0

    @BeforeAll
    fun configureRestAssured() {
        RestAssured.port = port
        val requestSpecification = RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .build()
        RestAssured.requestSpecification = requestSpecification
    }
}