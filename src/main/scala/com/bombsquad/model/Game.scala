package com.bombsquad.model

case class Game(id: String = s"GAME-${System.currentTimeMillis()}",
                username: String = s"GUEST_USER-${System.currentTimeMillis()}",
                workflow: GameWorkflow = GameWorkflow(),
                board: Board = BoardFactory.createWithRandomlyBuriedBombs()) {
  def start(): Unit = workflow.moveTo(Running)

  def pause(): Unit = workflow.moveTo(Paused)

  def cancel(): Unit = workflow.moveTo(Cancelled)

  def flagCell(row: Int, col: Int): Unit = {
    if (workflow.currentState != Running)
      throw new IllegalStateException(s"Cannot flag/unflag cell, state must be Running but it is ${workflow.currentState}")

    board.flag(row, col)
  }

  def unCoverCell(row: Int, col: Int): Unit = {
    if (workflow.currentState != Running)
      throw new IllegalStateException(s"Cannot uncover cell, state must be Running but it is ${workflow.currentState}")

    board.unCoverAndCheckForBomb(row, col).map { hasBomb =>
      if (hasBomb)
        workflow.moveTo(Lost)
      else if (board.allSafeCellsAreUnCovered())
        workflow.moveTo(Won)
    }
  }

  def isFinished: Boolean = workflow.currentState == Won || workflow.currentState == Lost || workflow.currentState == Cancelled
}