package com.bombsquad.model

case class Game(id: String, workflow: GameWorkflow = GameWorkflow(), user: User, board: Board = Board.createWithRandomlyBuriedBombs()) {
  require(id != null && !id.isEmpty, "id is needed")
}

object Game {

  def start(game: Game): Unit = game.workflow.moveTo(Running)

  def pause(game: Game): Unit = game.workflow.moveTo(Paused)

  def stop(game: Game, stopCause: GameStoppedState): Unit = game.workflow.moveTo(stopCause)

  def flagCell(game: Game, row: Int, col: Int): Unit = {
    if (game.workflow.currentState != Running)
      throw new IllegalStateException(s"Cannot flag/unflag cell, state must be Running but it is ${game.workflow.currentState}")

    game.board.flag(row, col)
  }

  def unCoverCell(game: Game, row: Int, col: Int): Unit = {
    if (game.workflow.currentState != Running)
        throw new IllegalStateException(s"Cannot uncover cell, state must be Running but it is ${game.workflow.currentState}")

    game.board.unCoverAndCheckForBomb(row, col).map { hasBomb =>
      if (hasBomb)
        game.workflow.moveTo(Lost)
      else
        if (game.board.isAllUnCovered())
          game.workflow.moveTo(Won)
    }
  }
}
