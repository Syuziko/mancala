package com.bol.mancala.infra.adapters.controller

import com.bol.mancala.domain.failures.ValidationException
import com.bol.mancala.domain.failures.NotFoundException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI

@RestControllerAdvice
internal class ExceptionHandler {
    private val log = KotlinLogging.logger { }

    @ExceptionHandler(value = [NotFoundException::class])
    fun handleNotFoundException(exception: NotFoundException): ProblemDetail =
        ProblemDetail.forStatus(HttpStatus.NOT_FOUND).apply {
            type = URI.create("https://mancala/errors/${exception.code.name.lowercase()}")
            title = exception.code.name
        }.also { log.debug(exception) { exception.message } }

    @ExceptionHandler(value = [ValidationException::class])
    fun handleValidationException(exception: ValidationException): ProblemDetail =
        ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY).apply {
            type = URI.create("https://mancala/errors/${exception.code.name.lowercase()}")
            title = exception.code.name
        }.also { log.debug(exception) { exception.message } }

    @ExceptionHandler(value = [Throwable::class])
    fun handleInternalException(exception: Throwable): ProblemDetail =
        ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR).apply {
            type = URI.create("https://mancala/errors/internal_error")
            title = "INTERNAL_ERROR"
        }.also { log.debug(exception) { exception.message } }
}