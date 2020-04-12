package com.bombsquad.model

import org.scalatest.{FlatSpec, Matchers}

class GameSpec extends FlatSpec with Matchers {

  "A NotRunning Game" should "fail when trying to uncover or flag a cell" in {
    val game = Game()
    a [IllegalStateException] should be thrownBy {
      game.unCoverCell(0, 0)
    }
    a [IllegalStateException] should be thrownBy {
      game.flagCell(0, 0)
    }
  }

  "Game" should "be able to be cancelled" in {
    val game = Game()
    game.start()
    game.cancel()
    game.isFinished should be (true)
    game.workflow.currentState should be (Cancelled.name)
  }

  "Game" should "be won by a guest user when all safe cells are uncovered" in {
    /*
        Given this initial Board:

            0 1 2 3
           --------
        0 | C C C B
        1 | B C C C
        2 | C C B C
        3 | C C C C

        References:
        C = Cell is Covered
        U = Cell is Uncovered
        B = Cell has a Bomb
        F = Cell is Flagged
        Digit = Number of surrounding bombs for this cell
    */

    // Game is created, with some bombs
    val game = Game(board = BoardFactory.createWithSpecificBuriedBombs(4, 4, Set(
      (0, 3),
      (1, 0),
      (2, 2)
    )))
    game.workflow.currentState should be (NotStarted.name)

    // Game starts
    game.start()
    game.workflow.currentState should be (Running.name)

    // User uncovers cell on (0, 0)
    game.unCoverCell(0, 0)

    /*
        Now the board should be like this:

            0 1 2 3
           --------
        0 | U C C B
        1 | B C C C
        2 | C C B C
        3 | C C C C
    */
    game.board.cellAt(0, 0).map(_.covered should be (false))

    // User flag cell on (1, 0)
    game.flagCell(1, 0)

    /*
        Now the board should be like this:

            0 1 2 3
           --------
        0 | U C C B
        1 | F C C C
        2 | C C B C
        3 | C C C C
    */
    game.board.cellAt(1, 0).map(_.flagged should be (true))

    // User uncovers cell on (3, 0)
    game.unCoverCell(3, 0)

    /*
        Now the board should be like this:

            0 1 2 3
           --------
        0 | U C C B
        1 | F C C C
        2 | 1 2 B C
        3 | U 1 C C
    */
    game.board.cellAt(3, 0).map(_.covered should be (false))
    game.board.cellAt(3, 1).map(_.surroundingBombs should be (1))
    game.board.cellAt(2, 1).map(_.surroundingBombs should be (2))
    game.board.cellAt(2, 0).map(_.surroundingBombs should be (1))

    // Game is paused for 1 sec
    game.pause()
    Thread.sleep(1000)
    game.workflow.currentState should be (Paused.name)

    // Game is restarted
    game.start()
    Thread.sleep(1000)
    game.workflow.currentState should be (Running.name)

    // User uncovers remaining cells
    game.unCoverCell(3, 3)
    game.unCoverCell(3, 2)
    game.unCoverCell(2, 3)
    game.unCoverCell(1, 1)
    game.unCoverCell(1, 2)
    game.unCoverCell(1, 3)
    game.unCoverCell(0, 1)
    game.unCoverCell(0, 2)

    /*
        Now the board should be like this, and user finished it (won)

            0 1 2 3
           --------
        0 | U U U B
        1 | F U U U
        2 | 1 2 B U
        3 | U 1 U U
    */
    game.isFinished should be (true)
    game.workflow.currentState should be (Won.name)
  }

  "Game" should "be lost by a guest user when a cell with a bomb is uncovered" in {
    /*
        Given this initial Board:

            0 1 2 3
           --------
        0 | C C C B
        1 | B C C C
        2 | C C B C
        3 | C C C C

        References:
        C = Cell is Covered
        U = Cell is Uncovered
        B = Cell has a Bomb
        F = Cell is Flagged
        Digit = Number of surrounding bombs for this cell
    */

    // Game is created, with some bombs
    val game = Game(board = BoardFactory.createWithSpecificBuriedBombs(4, 4, Set(
      (0, 3),
      (1, 0),
      (2, 2)
    )))
    game.workflow.currentState should be (NotStarted.name)

    // Game starts
    game.start()
    game.workflow.currentState should be (Running.name)

    // User uncovers cell with bomb on (1, 0)
    game.unCoverCell(1, 0)

    game.isFinished should be (true)
    game.workflow.currentState should be (Lost.name)
  }
}
