package com.bombsquad.model

case class Cell(covered: Boolean = true,
                hasBomb: Boolean = false,
                flagged: Boolean = false,
                surroundingBombs: Int = 0) {
}

object CellUtils {
  def safeAndCovered(cell: Cell): Boolean = cell.covered && !cell.hasBomb && !cell.flagged
}


