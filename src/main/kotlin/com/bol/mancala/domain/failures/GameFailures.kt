package com.bol.mancala.domain.failures

enum class ErrorCode {
    GAME_NOT_FOUND,
    INVALID_GAME_ID,
    PIT_EMPTY,
    PLAYER_ID_INVALID,
    PLAYER_NAME_INVALID,
    UNSUPPORTED_PIT
    ;
}

internal abstract class NotFoundException(val code: ErrorCode, message: String) : RuntimeException(message)

abstract class ValidationException(val code: ErrorCode, message: String) : IllegalArgumentException(message)