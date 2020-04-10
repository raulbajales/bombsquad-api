package com.bombsquad.model

case class Cell(covered: Boolean = true, hasBomb: Boolean = false, flagged: Boolean = false, surroundingBombs: Int = 0) {
  def isSafeAndCovered: Boolean = covered && !hasBomb && !flagged
}


