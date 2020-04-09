package com.bombsquad.model

import com.bombsquad.AppConf

import scala.annotation.tailrec

case class Board(matrix: Array[Array[Cell]], bombs: Int) {
  require(matrix != null, "matrix is required")
  require(bombs > 0, "You need at least 1 bomb on the board, no fun otherwise")

  def rows: Int = matrix.size

  def cols: Int = matrix(0).size

  def totalCells: Int = rows * cols

  require(bombs < totalCells, "Sorry, too much bombs")

  def inBounds(col: Int, row: Int): Boolean = (col >= 0 && col < cols) && (row >= 0 && col < rows)

  def isAllUnCovered(): Boolean = matrix.map(_.count(_.status == Uncovered)).sum == (totalCells - bombs)

  def cellAt(row: Int, col: Int): Option[Cell] = {
    if (inBounds(row, col))
      Some(matrix(row)(col))
    else
      None
  }

  def surroundingBombs(row: Int, col: Int): Int = Array(
      cellAt(row - 1, col - 1),
      cellAt(row - 1, col),
      cellAt(row - 1, col + 1),
      cellAt(row, col - 1),
      cellAt(row, col + 1),
      cellAt(row + 1, col - 1),
      cellAt(row + 1, col),
      cellAt(row + 1, col + 1)
  ).map(_.count(_.hasBomb)).sum

  def surroundingsHasBomb(row: Int, col: Int): Boolean = surroundingBombs(row, col) > 0

  def flag(row: Int, col: Int): Unit = cellAt(row, col).map { cell =>
    if (!Array(Covered, Flagged).contains(cell.status))
      throw new IllegalStateException(s"Cannot flag/unflag cell ${cell}")
    matrix(row)(col) = cell.copy(status = if (cell.status == Flagged) Covered else Flagged)
  }

  def unCoverAndCheckForBomb(row: Int, col: Int): Option[Boolean] = {
    def unCover(board: Board, row: Int, col: Int): Unit = {
      if (board.inBounds(col, row)) {
        cellAt(row, col).map { cell =>
          if (!cell.hasBomb && cell.status != Flagged && !board.surroundingsHasBomb(row, col)) {
            board.matrix(row)(col) = cell.copy(status = Uncovered)
            unCover(board, row - 1, col - 1)
            unCover(board, row - 1, col)
            unCover(board, row - 1, col + 1)
            unCover(board, row, col - 1)
            unCover(board, row, col + 1)
            unCover(board, row + 1, col - 1)
            unCover(board, row + 1, col)
            unCover(board, row + 1, col + 1)
          }
        }
      }
    }

    cellAt(row, col).map { cell =>
      if (cell.status != Covered)
        throw new IllegalStateException(s"Cannot uncover cell ${cell}")

      unCover(this, row, col)

      cell.hasBomb
    }
  }
}

object Board {

  def createWithRandomlyBuriedBombs(rows: Int = AppConf.defaultRows, cols: Int = AppConf.defaultCols, bombs: Int = AppConf.defaultBombs): Board = {
    val board = Board(Array.ofDim[Cell](rows, cols), bombs)
    for (row <- 0 until board.rows)
      for (col <- 0 until board.cols)
        board.matrix(row)(col) = Cell(status = Covered)
    spreadBombs(board)
    board
  }

  def createWithSpecificBuriedBombs(rows: Int = AppConf.defaultRows, cols: Int = AppConf.defaultCols, buriedBombs: Set[(Int, Int)]): Board = {
    val board = Board(Array.ofDim[Cell](rows, cols), buriedBombs.size)
    for (row <- 0 until board.rows)
      for (col <- 0 until board.cols)
        board.matrix(row)(col) = Cell(Covered, buriedBombs.contains((row, col)))
    board
  }

  def spreadBombs(board: Board): Unit = {
    val random = new scala.util.Random

    @tailrec
    def buryBomb(remaining: Int): Unit = {
      val row = random.nextInt(board.rows)
      val col = random.nextInt(board.cols)
      if (remaining > 0)
        if (board.matrix(row)(col).hasBomb)
          buryBomb(remaining)
        else {
          board.matrix(row)(col) = Cell(hasBomb = true)
          buryBomb(remaining - 1)
        }
    }

    buryBomb(board.bombs)
  }
}

