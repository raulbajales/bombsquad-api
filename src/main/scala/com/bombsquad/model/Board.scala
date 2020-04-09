package com.bombsquad.model

import com.bombsquad.AppConf

import scala.annotation.tailrec

case class Board(matrix: Array[Array[Cell]]) {
  require(matrix != null, "matrix is required")

  def rows: Int = matrix.size

  def cols: Int = matrix(0).size

  def totalCells: Int = rows * cols

  def inBounds(row: Int, col: Int): Boolean = (col >= 0 && col < cols) && (row >= 0 && row < rows)

  def isAllUnCovered(): Boolean = {
    matrix.map(
      _.count(
        _.status == Covered
      )
    ).sum == 0
  }

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
    ).map(_.count(_.status == HasBomb)).sum
  }

  def flag(row: Int, col: Int): Unit = cellAt(row, col).map { cell =>
    if (!Array(Covered, Flagged).contains(cell.status))
      throw new IllegalStateException(s"Cannot flag/unflag cell ${cell}")
    matrix(row)(col) = cell.copy(status = if (cell.status == Flagged) Covered else Flagged)
  }

  def unCoverAndCheckForBomb(row: Int, col: Int): Option[Boolean] = {
    def unCover(board: Board, row: Int, col: Int): Unit = {
      cellAt(row, col).map { cell =>
        val surroundingBombs = board.surroundingBombs(row, col)
        if (cell.status == Covered)
          if (surroundingBombs == 0) {
            board.matrix(row)(col) = cell.copy(status = Uncovered)
            unCover(board, row - 1, col - 1)
            unCover(board, row - 1, col)
            unCover(board, row - 1, col + 1)
            unCover(board, row, col - 1)
            unCover(board, row, col + 1)
            unCover(board, row + 1, col - 1)
            unCover(board, row + 1, col)
            unCover(board, row + 1, col + 1)
          } else {
            board.matrix(row)(col) = cell.copy(status = HasSurroundingBombs(surroundingBombs))
          }
      }
    }

    cellAt(row, col).map { cell =>
      if (cell.status != Covered)
        throw new IllegalStateException(s"Cannot uncover cell ${cell}")
      unCover(this, row, col)
      cell.status == HasBomb
    }
  }

  def countByStatus(status: CellStatus): Int = matrix.map(_.count(_.status == status)).sum
}

object Board {

  def createWithRandomlyBuriedBombs(rows: Int = AppConf.defaultRows, cols: Int = AppConf.defaultCols, bombs: Int = AppConf.defaultBombs): Board = {
    require(bombs < (rows * cols), "Too much bombs")
    val board = fill(newEmptyBoard(rows, cols))((_, _) => Cell(Covered))
    spreadBombs(board, bombs)
    board
  }

  def createWithSpecificBuriedBombs(rows: Int = AppConf.defaultRows, cols: Int = AppConf.defaultCols, bombs: Set[(Int, Int)]): Board = {
    require(bombs.size < (rows * cols), "Too much bombs")
    fill(newEmptyBoard(rows, cols))((row, col) => Cell(if (bombs.contains((row, col))) HasBomb else Covered))
  }

  def createWithoutBombs(rows: Int = AppConf.defaultRows, cols: Int = AppConf.defaultCols): Board =
    fill(newEmptyBoard(rows, cols))((_, _) => Cell(Covered))

  private def spreadBombs(board: Board, bombs: Int): Unit = {
    val seed = new scala.util.Random

    @tailrec
    def buryBomb(remaining: Int): Unit = {
      val row = seed.nextInt(board.rows)
      val col = seed.nextInt(board.cols)
      if (remaining > 0)
        if (board.matrix(row)(col).status == HasBomb)
          buryBomb(remaining)
        else {
          board.matrix(row)(col) = Cell(HasBomb)
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

