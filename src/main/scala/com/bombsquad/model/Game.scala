package com.bombsquad.model

import com.bombsquad.AppConf
import org.mongodb.scala.bson.ObjectId

case class Game(_id: ObjectId = new ObjectId(),
                username: String = s"GUEST_USER-${System.currentTimeMillis()}",
                workflow: GameWorkflow = GameWorkflow(),
                board: Board = BoardFactory.createWithRandomlyBuriedBombs()) {
  def start(): Unit = workflow.moveTo(Running)

  def pause(): Unit = workflow.moveTo(Paused)

  def cancel(): Unit = workflow.moveTo(Cancelled)

  def flagCell(row: Int, col: Int): Unit = {
    if (workflow.currentState != Running.name)
      throw new IllegalStateException(s"Cannot flag/unflag cell, state must be Running but it is ${workflow.currentState}")

    board.flag(row, col)
  }

  def unCoverCell(row: Int, col: Int): Unit = {
    if (workflow.currentState != Running.name)
      throw new IllegalStateException(s"Cannot uncover cell, state must be Running but it is ${workflow.currentState}")

    board.unCoverAndCheckForBomb(row, col).map { hasBomb =>
      if (hasBomb)
        workflow.moveTo(Lost)
      else if (board.allSafeCellsAreUnCovered())
        workflow.moveTo(Won)
    }
  }

  def isFinished: Boolean = Set(Won.name, Lost.name, Cancelled.name).contains(workflow.currentState)
}

case class GameRequest(rows: Int = AppConf.gameDefaultRows, cols: Int = AppConf.gameDefaultCols, bombs: Int = AppConf.gameDefaultBombs)

case class GameList(gameIds: List[String] = List())