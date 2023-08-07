package com.bol.mancala.domain.service

import com.bol.mancala.domain.failures.ValidationException
import com.bol.mancala.domain.failures.ErrorCode
import com.bol.mancala.domain.models.Game
import org.springframework.stereotype.Service

@Service
internal class GameService {

    fun play(game: Game, playerIndex: Int, pitIndex: Int) {
        val playerRow = game.playerRow(playerIndex)
        val opponentRow = game.opponentPlayerRow(playerIndex)
        val pit = playerRow.pitAt(pitIndex)

        if (pit.isMancala()) throw UnsupportedPitForSowOperationException()
        if (pit.isEmpty()) throw EmptyPitException(pit.toString())

        sowPitStones(pit, pitIndex, playerRow, opponentRow)

        if (game.board.isAnyRowEmpty()) {
            game.endGame()
        }
    }

    private fun sowPitStones(
        pit: Game.Board.Rows.Row.Pit,
        pitIndex: Int,
        playerRow: Game.Board.Rows.Row,
        opponentRow: Game.Board.Rows.Row,
    ) {
        var stonesToSow = pit.popStones()

        var finalPitIndex: Int = -1
        var isOwnPit = false
        while (stonesToSow != 0) {
            var i = getNextPitIndex(pitIndex, finalPitIndex)

            while (i <= Game.Board.Rows.SMALL_PITS_COUNT && stonesToSow != 0) {
                playerRow.pitAt(i).addStone()
                i++
                stonesToSow--
            }
            finalPitIndex = i - 1
            if (stonesToSow == 0) {
                isOwnPit = true
                break
            }

            i = 0
            while (i <= (Game.Board.Rows.SMALL_PITS_COUNT - 1) && stonesToSow != 0) {
                opponentRow.pitAt(i).addStone()
                i++
                stonesToSow--
            }
            finalPitIndex = i - 1
        }

        captureOpponentStonesIfFinalPitEmpty(playerRow, finalPitIndex, isOwnPit, opponentRow)
    }

    private fun captureOpponentStonesIfFinalPitEmpty(
        playerRow: Game.Board.Rows.Row,
        finalPitIndex: Int,
        isOwnPit: Boolean,
        opponentRow: Game.Board.Rows.Row,
    ) {
        with(playerRow.pitAt(finalPitIndex)) {
            if (isOwnPit && this.hasSingleStone()) {
                val mancala = playerRow.mancala()
                val opponentPit = opponentRow.pits[Game.Board.Rows.SMALL_PITS_COUNT - finalPitIndex - 1]
                mancala.addStones(this.popStones() + opponentPit.popStones())
            }
        }

    }

    private fun getNextPitIndex(pitId: Int, endPit: Int) = if (endPit != -1) 0 else pitId + 1

    class EmptyPitException(pit: String) : ValidationException(ErrorCode.PIT_EMPTY, " Pit $pit is empty.")
    class UnsupportedPitForSowOperationException :
        ValidationException(ErrorCode.UNSUPPORTED_PIT, "Manacala stones can't be sown.")
}