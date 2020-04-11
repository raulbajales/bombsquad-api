package com.bombsquad.model

import com.bombsquad.AppConf

import scala.annotation.tailrec

case class Board(matrix: Array[Array[Cell]]) {
  require(matrix != null, "matrix is required")
  require(matrix.size > 0, "matrix should have at least one row")

  def rows: Int = matrix.size

  def cols: Int = matrix(0).size

  def totalCells: Int = rows * cols

  def inBounds(row: Int, col: Int): Boolean = (col >= 0 && col < cols) && (row >= 0 && row < rows)

  def allSafeCellsAreUnCovered(): Boolean = matrix.map(_.count(_.isSafeAndCovered)).sum == 0

  def cellAt(row: Int, col: Int): Option[Cell] = {
    if (inBounds(row, col))
      Some(matrix(row)(col))
    else
      None
  }

  def surroundingBombs(row: Int, col: Int): Int = {
    Array(
      cellAt(row - 1, col - 1),
      cellAt(row - 1, col),
      cellAt(row - 1, col + 1),
      cellAt(row, col - 1),
      cellAt(row, col + 1),
      cellAt(row + 1, col - 1),
      cellAt(row + 1, col),
      cellAt(row + 1, col + 1)
    ).map(_.count(_.hasBomb)).sum
  }

  def flag(row: Int, col: Int): Unit = cellAt(row, col).map { cell =>
    if (!cell.covered)
      throw new IllegalStateException(s"Cannot flag/unflag cell ${cell} because it's uncovered")
    matrix(row)(col) = cell.copy(flagged = !cell.flagged)
  }

  def unCoverAndCheckForBomb(row: Int, col: Int): Option[Boolean] = {
    def unCover(row: Int, col: Int): Unit = cellAt(row, col).map { cell =>
      if (cell.isSafeAndCovered) {
        val newCell = cell.copy(covered = false, surroundingBombs = surroundingBombs(row, col))
        matrix(row)(col) = newCell
        if (newCell.surroundingBombs == 0) {
          unCover(row - 1, col - 1)
          unCover(row - 1, col)
          unCover(row - 1, col + 1)
          unCover(row, col - 1)
          unCover(row, col + 1)
          unCover(row + 1, col - 1)
          unCover(row + 1, col)
          unCover(row + 1, col + 1)
        }
      }
    }

    cellAt(row, col).map { cell =>
      if (!cell.covered)
        throw new IllegalStateException(s"Cannot uncover cell ${cell}")
      unCover(row, col)
      cell.hasBomb
    }
  }
}

object BoardFactory {

  def createWithRandomlyBuriedBombs(rows: Int = AppConf.defaultRows, cols: Int = AppConf.defaultCols, bombs: Int = AppConf.defaultBombs): Board = {
    require(bombs < (rows * cols), "Too much bombs")
    val board = fill(newEmptyBoard(rows, cols))((_, _) => Cell())
    spreadBombs(board, bombs)
    board
  }

  def createWithSpecificBuriedBombs(rows: Int = AppConf.defaultRows, cols: Int = AppConf.defaultCols, bombs: Set[(Int, Int)]): Board = {
    require(bombs.size < (rows * cols), "Too much bombs")
    fill(newEmptyBoard(rows, cols))((row, col) => Cell(hasBomb = bombs.contains((row, col))))
  }

  def createWithoutBombs(rows: Int = AppConf.defaultRows, cols: Int = AppConf.defaultCols): Board =
    fill(newEmptyBoard(rows, cols))((_, _) => Cell())

  private def spreadBombs(board: Board, bombs: Int): Unit = {
    val seed = new scala.util.Random

    @tailrec
    def buryBomb(remaining: Int): Unit = {
      val row = seed.nextInt(board.rows)
      val col = seed.nextInt(board.cols)
      if (remaining > 0)
        if (board.matrix(row)(col).hasBomb)
          buryBomb(remaining)
        else {
          board.matrix(row)(col) = Cell(hasBomb = true)
          buryBomb(remaining - 1)
        }
    }

    buryBomb(bombs)
  }

  private def newEmptyBoard(rows: Int, cols: Int): Board = Board(Array.ofDim[Cell](rows, cols))

  private def fill(board: Board)(f: (Int, Int) => Cell): Board = {
    for (row <- 0 until board.rows)
      for (col <- 0 until board.cols)
        board.matrix(row)(col) = f(row, col)
    board
  }
}

