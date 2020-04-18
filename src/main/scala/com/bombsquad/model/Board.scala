package com.bombsquad.model

import com.bombsquad.AppConf

import scala.annotation.tailrec

case class Board(var matrix: List[List[Cell]]) {
  require(matrix != null, "matrix is required")
  require(matrix.size > 0, "matrix should have at least one row")

  def rows: Int = matrix.size

  def cols: Int = matrix(0).size

  def totalCells: Int = rows * cols

  def inBounds(row: Int, col: Int): Boolean = (col >= 0 && col < cols) && (row >= 0 && row < rows)

  def allSafeCellsAreUnCovered(): Boolean = matrix.map(_.count(CellUtils.safeAndCovered(_))).sum == 0

  def cellAt(row: Int, col: Int): Option[Cell] = {
    if (inBounds(row, col))
      Some(matrix(row)(col))
    else
      None
  }

  def replaceCell(row: Int, col: Int, cell: Cell): Unit = matrix = matrix.updated(row, matrix(row).updated(col, cell))

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
    replaceCell(row, col, cell.copy(flagged = !cell.flagged))
  }

  def unCoverAndCheckForBomb(row: Int, col: Int): Option[Boolean] = {
    def unCover(row: Int, col: Int): Unit = cellAt(row, col).map { cell =>
      if (CellUtils.safeAndCovered(cell)) {
        val newCell = cell.copy(covered = false, surroundingBombs = surroundingBombs(row, col))
        replaceCell(row, col, newCell)
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

  def createWithRandomlyBuriedBombs(gameRequest: GameRequest = GameRequest()): Board = {
    require(gameRequest.bombs < (gameRequest.rows * gameRequest.cols), "Too much bombs")
    val board = fill(newEmptyBoard(gameRequest.rows, gameRequest.cols))((_, _) => Cell())
    spreadBombs(board, gameRequest.bombs)
    board
  }

  def createWithSpecificBuriedBombs(rows: Int = AppConf.gameDefaultRows, cols: Int = AppConf.gameDefaultCols, bombs: Set[(Int, Int)]): Board = {
    require(bombs.size < (rows * cols), "Too much bombs")
    fill(newEmptyBoard(rows, cols))((row, col) => Cell(hasBomb = bombs.contains((row, col))))
  }

  def createWithoutBombs(rows: Int = AppConf.gameDefaultRows, cols: Int = AppConf.gameDefaultCols): Board =
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
          board.replaceCell(row, col, Cell(hasBomb = true))
          buryBomb(remaining - 1)
        }
    }

    buryBomb(bombs)
  }

  private def newEmptyBoard(rows: Int, cols: Int): Board = Board(
    (0 until rows).map(_ => (
      0 until cols).map(_ => Cell()).toList
    ).toList
  )

  private def fill(board: Board)(f: (Int, Int) => Cell): Board = {
    for (row <- 0 until board.rows)
      for (col <- 0 until board.cols)
        board.replaceCell(row, col, f(row, col))
    board
  }
}

