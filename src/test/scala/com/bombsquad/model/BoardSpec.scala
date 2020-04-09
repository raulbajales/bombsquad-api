package com.bombsquad.model

import org.scalatest.{FlatSpec, Matchers}

class BoardSpec extends FlatSpec with Matchers {

  "Board creation" should "fail if no matrix is set" in {
    a [IllegalArgumentException] should be thrownBy {
      Board(null, 10)
    }
  }

  "Board creation" should "fail without bombs" in {
    a [IllegalArgumentException] should be thrownBy {
      Board.createWithRandomlyBuriedBombs(bombs = 0)
    }
  }

  "Board creation" should "have spread all the configured bombs" in {
    Board.createWithRandomlyBuriedBombs(bombs = 30).matrix.map(_.count(_.hasBomb)).sum should be (30)
  }

  "Board creation" should "fail if too much bombs" in {
    a [IllegalArgumentException] should be thrownBy {
      Board.createWithRandomlyBuriedBombs(bombs = 1000)
    }
  }

  "Board creation" should "correctly calculate rows, cols and totalCells" in {
    val board = Board.createWithRandomlyBuriedBombs(5, 5)
    board.rows should be (5)
    board.cols should be (5)
    board.totalCells should be (25)
  }

  "Board" should "be able to get a cell based on in bounds coordinates, or fail otherwise" in {
    val board = Board.createWithRandomlyBuriedBombs(5, 5)
    board.cellAt(4, 4) should not be (null)
    board.cellAt(100, 100) should be (None)
  }

  "Board" should "know when all non-bomb cells are uncovered" in {
    val board = Board.createWithRandomlyBuriedBombs(5, 5)
    for (row <- 0 until 5)
      for (col <- 0 until 5) board.cellAt(row, col).map { cell =>
        if (!cell.hasBomb && cell.status != Flagged)
          board.matrix(row)(col) = cell.copy(status = Uncovered)
      }
    board.isAllUnCovered() should be (true)
  }

  "Board" should "know when coordinates are in bounds" in {
    val board = Board.createWithRandomlyBuriedBombs(5, 5)
    board.inBounds(0, 0) should be (true)
    board.inBounds(-1, -1) should be (false)
    board.inBounds(5, 5) should be (false)
    board.inBounds(4, 4) should be (true)
  }

  "Board" should "be able to flag a covered cell, and get it back to covered when trying to flag it again" in {
    val board = Board.createWithRandomlyBuriedBombs(5, 5)
    board.flag(0, 0)
    board.cellAt(0, 0).map(_.status should be (Flagged))
    board.flag(0, 0)
    board.cellAt(0, 0).map(_.status should be (Covered))
  }

  "Board" should "fail trying to flag an uncovered cell" in {
    val board = Board.createWithRandomlyBuriedBombs(5, 5)
    board.matrix(0)(0) = Cell(Uncovered)
    a [IllegalStateException] should be thrownBy {
      board.flag(0, 0)
    }
  }

  /*
  Given this board:

      0 1 2 3 4
     ----------
  0 | C C C C C
  1 | C C C C C
  2 | C C C C C
  3 | C C C B C
  4 | C C C C C

  When I try to uncover cell in (row = 0, col = 0), then I should get this board:

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
  B = Cell has a Bomb and is Covered
  Digit = Number of surrounding bombs for this cell
  */
  "Board" should "be able to uncover surrounding cells when uncovering a safe cell" in {
    val board = Board.createWithSpecificBuriedBombs(5, 5, Set((3, 3)))
    board.unCoverAndCheckForBomb(1, 1)
    Array(
      board.cellAt(0, 0),
      board.cellAt(0, 1),
      board.cellAt(0, 2),
      board.cellAt(0, 3),
      board.cellAt(0, 4),

      board.cellAt(1, 0),
      board.cellAt(1, 1),
      board.cellAt(1, 2),
      board.cellAt(1, 3),
      board.cellAt(1, 4),

      board.cellAt(2, 0),
      board.cellAt(2, 1),
      board.cellAt(2, 2),
      board.cellAt(2, 3),
      board.cellAt(2, 4),

      board.cellAt(3, 0),
      board.cellAt(3, 1),
      board.cellAt(3, 2),

      board.cellAt(4, 0),
      board.cellAt(4, 1),
      board.cellAt(4, 2)
    ).map(_.count(_.status == Uncovered)).sum should be (21)

    Array(
      (2, 2),
      (2, 3),
      (2, 4),
      (3, 2),
      (4, 2)
    ).map(pair => board.surroundingBombs(pair._1, pair._2) should be (1))
  }
}