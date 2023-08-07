package com.bol.mancala.domain.models

import com.bol.mancala.domain.failures.ValidationException
import com.bol.mancala.domain.failures.ErrorCode
import java.util.UUID
import kotlin.math.abs

internal data class Game private constructor(
    val gameId: GameId,
    val players: Players,
    val board: Board,
    var status: Status = Status.IN_PROGRESS,
) {

    data class Players(private val players: List<Player>) {
        init {
            if (players.size != 2) throw InvalidPlayersNumberException()
        }

        fun playerAt(index: Int): Player {
            if (index !in 0..1) throw PlayerIndexOutOfBoundsException()
            return players[index]
        }

        companion object {
            fun of(player1: Player, player2: Player) = Players(listOf(player1, player2))
        }

        @JvmInline
        value class Player private constructor(private val value: String) {
            init {
                if (value.isBlank()) throw BlankPlayerNameException()
                if (value.length > PLAYER_NAME_MAX_LENGTH) throw TooLongPlayerNameException()
            }

            fun name() = value

            companion object {
                fun of(value: String) = Player(value)

                const val PLAYER_NAME_MAX_LENGTH = 50
            }

            class BlankPlayerNameException :
                ValidationException(ErrorCode.PLAYER_NAME_INVALID, "Player name is mandatory")

            class TooLongPlayerNameException :
                ValidationException(ErrorCode.PLAYER_NAME_INVALID, "Player name should have max 50 characters")
        }

        class InvalidPlayersNumberException :
            ValidationException(ErrorCode.PLAYER_NAME_INVALID, "Number of players should be 2")

        class PlayerIndexOutOfBoundsException :
            ValidationException(ErrorCode.PLAYER_ID_INVALID, "Player id should be within rand 0..1")
    }

    data class Board private constructor(val rows: Rows) {

        data class Rows(val row1: Row, val row2: Row) {

            data class Row(val pits: List<Pit>) {
                fun pitAt(index: Int): Pit {
                    if (index !in 0 until TOTAL_PITS_COUNT) throw PitIdOutOfBoundsException()
                    return this.pits[index]
                }

                fun mancala(): MancalaPit = pitAt(TOTAL_PITS_COUNT - 1) as MancalaPit

                fun isAllSmallPitsEmpty(): Boolean = smallPits().all { it.isEmpty() }

                fun collectAllStones(): Int = smallPits().sumOf { it.popStones() }

                private fun smallPits() = this.pits.filterIsInstance<SmallPit>()

                abstract class Pit(var stones: Int) {
                    fun isEmpty(): Boolean = stones == 0
                    fun isMancala(): Boolean = this is MancalaPit
                    fun hasSingleStone(): Boolean = this is SmallPit && stones == 1
                    fun addStone() {
                        this.stones = this.stones + 1
                    }

                    fun popStones(): Int {
                        val stonesToSow = this.stones
                        this.clear()
                        return stonesToSow
                    }

                    private fun clear() {
                        this.stones = 0
                    }
                }

                class MancalaPit(stones: Int = MANCALA_STONES_AMOUNT) : Pit(stones) {
                    fun addStones(amount: Int) {
                        this.stones = this.stones + amount
                    }

                    override fun equals(other: Any?) = (this === other) ||
                            (other is MancalaPit) &&
                            stones == other.stones

                    override fun hashCode() = stones.hashCode()
                }

                class SmallPit(stones: Int = SMALL_PITS_STONES_AMOUNT) : Pit(stones) {
                    override fun equals(other: Any?) = (this === other) ||
                            (other is SmallPit) &&
                            stones == other.stones

                    override fun hashCode() = stones.hashCode()
                }

                companion object {
                    fun create(): Row {
                        var pitsList: List<Pit> = arrayListOf()

                        repeat(SMALL_PITS_COUNT) {
                            pitsList = pitsList.plus(SmallPit())
                        }
                        pitsList = pitsList.plus(MancalaPit())
                        return Row(pitsList)
                    }

                    const val SMALL_PITS_STONES_AMOUNT = 6
                    const val MANCALA_STONES_AMOUNT = 0
                    const val MANCALA_PIT_INDEX = 6
                }
            }

            companion object {
                fun create() = Rows(Row.create(), Row.create())
                fun of(row1: Row, row2: Row) = Rows(row1, row2)

                const val TOTAL_PITS_COUNT = 7
                const val SMALL_PITS_COUNT = 6
            }

            class PitIdOutOfBoundsException : IllegalArgumentException()
        }

        fun collectStonesIntoMancalas() {
            with(rows.row1) { mancala().addStones(collectAllStones()) }
            with(rows.row2) { mancala().addStones(collectAllStones()) }
        }

        fun isAnyRowEmpty() = this.rows.row1.isAllSmallPitsEmpty() || this.rows.row2.isAllSmallPitsEmpty()

        companion object {
            fun create() = Board(Rows.create())
            fun of(row1: Rows.Row, row2: Rows.Row): Board = Board(Rows.of(row1, row2))
        }
    }

    @JvmInline
    value class GameId private constructor(private val value: String) {

        init {
            if (value.isBlank()) throw BlankGameIdException()
        }

        override fun toString(): String = value
        fun toUuid(): UUID = UUID.fromString(value)

        companion object {
            fun of(id: String) = GameId(id)
        }

        class BlankGameIdException : ValidationException(ErrorCode.INVALID_GAME_ID, "Invalid game id.")
    }

    enum class Status {
        IN_PROGRESS,
        ENDED
    }

    companion object {
        fun of(id: GameId, players: Players, status: Status = Status.IN_PROGRESS) =
            Game(id, players, Board.create(), status)

        fun of(id: GameId, players: Players, board: Board, status: Status = Status.IN_PROGRESS) =
            Game(id, players, board, status)
    }

    fun playerRow(playerIndex: Int): Board.Rows.Row = when (playerIndex) {
        0 -> this.board.rows.row1
        1 -> this.board.rows.row2
        else -> throw Players.PlayerIndexOutOfBoundsException()
    }

    fun opponentPlayerRow(playerIndex: Int): Board.Rows.Row = playerRow(abs(playerIndex - 1))

    fun endGame() {
        this.status = Status.ENDED
        this.board.collectStonesIntoMancalas()
    }
}