package com.bol.mancala.infra.adapters.controller

import com.bol.mancala.domain.failures.ErrorCode
import com.bol.mancala.domain.failures.NotFoundException
import com.bol.mancala.domain.failures.ValidationException
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.net.URI

internal class ExceptionHandlerTest {

    private val underTest = ExceptionHandler()

    @Test
    fun `given NotFoundException, then 404 status code`() {
        val exception = TestNotFoundException(ErrorCode.GAME_NOT_FOUND)

        val result = underTest.handleNotFoundException(exception)

        result.status shouldBe HttpStatus.NOT_FOUND.value()
        result.title shouldBe ErrorCode.GAME_NOT_FOUND.name
        result.type shouldBe URI.create("https://mancala/errors/${ErrorCode.GAME_NOT_FOUND.name.lowercase()}")

    }

    @Test
    fun `given ValidationException, then 422 status code`() {
        val exception = TestValidationException(ErrorCode.PIT_EMPTY)

        val result = underTest.handleValidationException(exception)

        result.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY.value()
        result.title shouldBe ErrorCode.PIT_EMPTY.name
        result.type shouldBe URI.create("https://mancala/errors/${ErrorCode.PIT_EMPTY.name.lowercase()}")
    }

    @Test
    fun `given RuntimeException, then 500 status code`() {
        val exception = RuntimeException()

        val result = underTest.handleInternalException(exception)

        result.status shouldBe HttpStatus.INTERNAL_SERVER_ERROR.value()
        result.title shouldBe "INTERNAL_ERROR"
        result.type shouldBe URI.create("https://mancala/errors/internal_error")
    }

    class TestNotFoundException(errorCode: ErrorCode) : NotFoundException(errorCode, errorCode.name)
    class TestValidationException(errorCode: ErrorCode) : ValidationException(errorCode, errorCode.name)
}