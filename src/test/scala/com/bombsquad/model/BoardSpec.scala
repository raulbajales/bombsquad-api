package com.bombsquad.model

import org.scalatest.{FlatSpec, Matchers}

class BoardSpec extends FlatSpec with Matchers {

  "Board creation" should "fail if no matrix is set" in {
    a [IllegalArgumentException] should be thrownBy {
      Board(null)
    }
  }

  "Board creation" should "have spread randomly all the configured bombs" in {
    Board.createWithRandomlyBuriedBombs(bombs = 30).countByStatus(HasBomb) should be (30)
  }

  "Board creation" should "have spread specifically all the configured bombs" in {
    val board = Board.createWithSpecificBuriedBombs(bombs = Set((0, 0), (1, 1), (2, 1)))
    board.cellAt(0, 0).map(_.status should be (HasBomb))
    board.cellAt(1, 1).map(_.status should be (HasBomb))
    board.cellAt(2, 1).map(_.status should be (HasBomb))
  }

  "Board creation" should "fail if too much bombs" in {
    a [IllegalArgumentException] should be thrownBy {
      Board.createWithRandomlyBuriedBombs(3, 3, 50)
    }
  }

  "Board creation" should "correctly calculate rows, cols and totalCells" in {
    val board = Board.createWithRandomlyBuriedBombs(5, 5)
    board.rows should be (5)
    board.cols should be (5)
    board.totalCells should be (25)
  }

  "Board" should "be able to get a cell based on in-bounds coordinates, or fail otherwise" in {
    val board = Board.createWithRandomlyBuriedBombs(5, 5)
    board.cellAt(4, 4) should not be (null)
    board.cellAt(100, 100) should be (None)
  }

  /*
      Given this Board:

          0 1 2 3
         --------
      0 | F U U U
      1 | U 1 2 2
      2 | U 2 B F
      3 | U 2 B 2

      Then the number of valid Uncovered safe cells should be 17

      References:
      C = Cell is Covered
      U = Cell is Uncovered
      B = Cell has a Bomb and is Covered
      F = Cell is Flagged
      Digit = Number of surrounding bombs for this cell
   */
  "Board" should "know when all safe cells are uncovered" in {
    val board = Board.createWithoutBombs(4, 4)
    board.matrix(0) = Array(Cell(Flagged), Cell(Uncovered), Cell(Uncovered), Cell(Uncovered))
    board.matrix(1) = Array(Cell(Uncovered), Cell(HasSurroundingBombs(1)), Cell(HasSurroundingBombs(2)), Cell(HasSurroundingBombs(2)))
    board.matrix(2) = Array(Cell(Uncovered), Cell(HasSurroundingBombs(2)), Cell(HasBomb), Cell(Flagged))
    board.matrix(3) = Array(Cell(Uncovered), Cell(HasSurroundingBombs(2)), Cell(HasBomb), Cell(HasSurroundingBombs(2)))
    board.isAllUnCovered() should be (true)
  }

  /*
    Given this Board:

        0 1 2
       ------
    0 | F U C
    1 | B 2 U
    2 | F 2 B

    The board should know the number of cells by status.

    References:
    C = Cell is Covered
    U = Cell is Uncovered
    B = Cell has a Bomb and is Covered
    F = Cell is Flagged
    Digit = Number of surrounding bombs for this cell
 */
  "Board" should "should know the number of cells by status" in {
    val board = Board.createWithoutBombs(3, 3)
    board.matrix(0) = Array(Cell(Flagged), Cell(Uncovered), Cell(Covered))
    board.matrix(1) = Array(Cell(HasBomb), Cell(HasSurroundingBombs(2)), Cell(Uncovered))
    board.matrix(2) = Array(Cell(Flagged), Cell(HasSurroundingBombs(2)), Cell(HasBomb))
    board.countByStatus(Flagged) should be (2)
    board.countByStatus(Uncovered) should be (2)
    board.countByStatus(Covered) should be (1)
    board.countByStatus(HasBomb) should be (2)
    board.countByStatus(HasSurroundingBombs(2)) should be (2)
  }

  "Board" should "know when coordinates are in bounds" in {
    val board = Board.createWithRandomlyBuriedBombs(5, 9)
    board.inBounds(0, 0) should be (true)
    board.inBounds(-1, -1) should be (false)
    board.inBounds(5, 5) should be (false)
    board.inBounds(4, 4) should be (true)
    board.inBounds(4, 8) should be (true)
    board.inBounds(4, 9) should be (false)
  }

  "Board" should "be able to flag a covered cell, and get it back to covered when trying to flag it again" in {
    val board = Board.createWithoutBombs()
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
      B = Cell has a Bomb and is Covered
  */
  "Board" should "be able to count surrounding bombs" in {
    val board = Board.createWithSpecificBuriedBombs(4, 4, Set((2, 2), (3, 1), (3, 2)))

    List(
      (1, 1),
      (1, 2),
      (1, 3),
      (2, 0),
      (3, 0)
    ).map(pair => board.surroundingBombs(pair._1, pair._2) should be (1))

    board.surroundingBombs(2, 1) should be (3)
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

      When I try to uncover cell in (row = 0, col = 0), then I should get this board (with 16 Uncovered cells):

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
    board.countByStatus(Uncovered) should be (16)
  }
}