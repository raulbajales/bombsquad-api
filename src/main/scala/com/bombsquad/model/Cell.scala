package com.bombsquad.model

sealed trait CellStatus
case object Covered extends CellStatus
case object Uncovered extends CellStatus
case object Flagged extends CellStatus
case object HasBomb extends CellStatus
case class HasSurroundingBombs(amount: Int) extends CellStatus

case class Cell(status: CellStatus = Covered)