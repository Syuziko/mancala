package com.bol.mancala.infra.adapters.persistence.entities

import java.util.UUID

internal class Game(
    val id: UUID,
    val firstPlayer: String,
    val secondPlayer: String,
    val board: Board,
    val status: String,
) {

    internal class Board(val rows: List<Row>) {
        class Row(val pits: List<Pit>) {
            class Pit(val stones: Int)
        }
    }
}