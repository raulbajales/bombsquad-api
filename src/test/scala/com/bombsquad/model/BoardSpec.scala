package com.bombsquad.model

import org.scalatest.{FlatSpec, Matchers}

class BoardSpec extends FlatSpec with Matchers {

  "Board creation" should "fail if no matrix is set" in {
    a [IllegalArgumentException] should be thrownBy {
      Board(null)
    }
  }

  "Board creation" should "have spread randomly all the configured bombs" in {
    BoardFactory.createWithRandomlyBuriedBombs(bombs = 30).matrix.map(_.count(_.hasBomb)).sum should be (30)
  }

  "Board creation" should "have spread specifically all the configured bombs" in {
    val board = BoardFactory.createWithSpecificBuriedBombs(bombs = Set((0, 0), (1, 1), (2, 1)))
    board.cellAt(0, 0).map(_.hasBomb should be (true))
    board.cellAt(1, 1).map(_.hasBomb should be (true))
    board.cellAt(2, 1).map(_.hasBomb should be (true))
  }

  "Board creation" should "fail if too much bombs" in {
    a [IllegalArgumentException] should be thrownBy {
      BoardFactory.createWithRandomlyBuriedBombs(3, 3, 50)
    }
  }

  "Board creation" should "correctly calculate rows, cols and totalCells" in {
    val board = BoardFactory.createWithRandomlyBuriedBombs(5, 5)
    board.rows should be (5)
    board.cols should be (5)
    board.totalCells should be (25)
  }

  "Board" should "be able to get a cell based on in-bounds coordinates, or fail otherwise" in {
    val board = BoardFactory.createWithRandomlyBuriedBombs(5, 5)
    board.cellAt(4, 4) should not be (null)
    board.cellAt(100, 100) should be (None)
  }

  "Board" should "know when all safe cells are uncovered" in {
    /*
        Given this Board:

            0 1 2 3
           --------
        0 | F U U U
        1 | U 1 2 2
        2 | U 2 B F
        3 | U 2 B 2

        Then all safe cells should be uncovered

        References:
        C = Cell is Covered
        U = Cell is Uncovered
        B = Cell has a Bomb
        F = Cell is Flagged
        Digit = Number of surrounding bombs for this cell
    */
    val board = BoardFactory.createWithoutBombs(4, 4)
    board.matrix(0) = Array(Cell(flagged = true), Cell(covered = false), Cell(covered = false), Cell(covered = false))
    board.matrix(1) = Array(Cell(covered = false), Cell(covered = false, surroundingBombs = 1), Cell(covered = false, surroundingBombs = 2), Cell(covered = false, surroundingBombs = 2))
    board.matrix(2) = Array(Cell(covered = false), Cell(covered = false, surroundingBombs = 2), Cell(hasBomb = true), Cell(flagged = true))
    board.matrix(3) = Array(Cell(covered = false), Cell(covered = false, surroundingBombs = 2), Cell(hasBomb = true), Cell(covered = false, surroundingBombs = 2))
    board.allSafeCellsAreUnCovered() should be (true)
  }

  "Board" should "know when coordinates are in bounds" in {
    val board = BoardFactory.createWithRandomlyBuriedBombs(5, 9)
    board.inBounds(0, 0) should be (true)
    board.inBounds(-1, -1) should be (false)
    board.inBounds(5, 5) should be (false)
    board.inBounds(4, 4) should be (true)
    board.inBounds(4, 8) should be (true)
    board.inBounds(4, 9) should be (false)
  }

  "Board" should "be able to flag a covered cell, and get it back to covered when trying to flag it again" in {
    val board = BoardFactory.createWithoutBombs()
    board.flag(0, 0)
    board.cellAt(0, 0).map(_.flagged should be (true))
    board.flag(0, 0)
    board.cellAt(0, 0).map(_.covered should be (true))
  }

  "Board" should "fail trying to flag an uncovered cell" in {
    val board = BoardFactory.createWithRandomlyBuriedBombs(5, 5)
    board.matrix(0)(0) = Cell(covered = false)
    a [IllegalStateException] should be thrownBy {
      board.flag(0, 0)
    }
  }

  "Board" should "be able to count surrounding bombs" in {
    /*
        Given this board:

            0 1 2 3
           --------
        0 | C C C C
        1 | C C C C
        2 | C C B C
        3 | C B B C

        When I try to count surrounding bombs for:
         a) (1, 1), (1, 2), (1, 3), (2, 0), (3, 0) => Result should be 1
         b) (2, 1) => Result should be 3

        References:
        C = Cell is Covered
        B = Cell has a Bomb
    */
    val board = BoardFactory.createWithSpecificBuriedBombs(4, 4, Set((2, 2), (3, 1), (3, 2)))

    List(
      (1, 1),
      (1, 2),
      (1, 3),
      (2, 0),
      (3, 0)
    ).map(pair => board.surroundingBombs(pair._1, pair._2) should be (1))

    board.surroundingBombs(2, 1) should be (3)
  }

  "Board" should "be able to uncover surrounding cells when uncovering a safe cell" in {
    /*
    Given this board:

            0 1 2 3 4
           ----------
        0 | C C C C C
        1 | C C C C C
        2 | C C C C C
        3 | C C C B C
        4 | C C C C C

        When I try to uncover cell in (row = 0, col = 0), then I should get this board
        (with 21 Uncovered cells, where 5 of these has surroundingBombs = 1):

            0 1 2 3 4
           ----------
        0 | U U U U U
        1 | U U U U U
        2 | U U 1 1 1
        3 | U U 1 B C
        4 | U U 1 C C

        References:
        C = Cell is Covered
        U = Cell is Uncovered
        B = Cell has a Bomb
        Digit = Number of surrounding bombs for this cell
    */
    val board = BoardFactory.createWithSpecificBuriedBombs(5, 5, Set((3, 3)))
    board.unCoverAndCheckForBomb(1, 1)
    board.matrix.map(_.count(!_.covered)).sum should be (21)
  }
}