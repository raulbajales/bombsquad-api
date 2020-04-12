package com.bombsquad.model

sealed trait GameState {
  val canMoveTo: Array[GameState] = Array()

  def activity(stopWatch: StopWatch): Unit = {}

  val name: String
}

sealed trait GameStoppedState extends GameState

case object NotStarted extends GameState {
  override val canMoveTo: Array[GameState] = Array(Running)
  override val name: String = "NOT_STARTED"
}

case object Running extends GameState {
  override val canMoveTo: Array[GameState] = Array(Paused, Cancelled, Won, Lost)

  override def activity(stopWatch: StopWatch): Unit = stopWatch.start()
  override val name: String = "RUNNING"
}

case object Paused extends GameState {
  override val canMoveTo: Array[GameState] = Array(Running, Cancelled)

  override def activity(stopWatch: StopWatch): Unit = stopWatch.stop()
  override val name: String = "PAUSED"
}

case object Cancelled extends GameStoppedState {
  override def activity(stopWatch: StopWatch): Unit = stopWatch.stop()
  override val name: String = "CANCELLED"
}

case object Won extends GameStoppedState {
  override def activity(stopWatch: StopWatch): Unit = stopWatch.stop()
  override val name: String = "WON"
}

case object Lost extends GameStoppedState {
  override def activity(stopWatch: StopWatch): Unit = stopWatch.stop()
  override val name: String = "LOST"
}

case class GameWorkflow(var currentState: String = NotStarted.name,
                        stopWatch: StopWatch = StopWatch()) {
  def moveTo(nextState: GameState): Unit = {
    if (!gameStateByName(currentState).canMoveTo.contains(nextState))
      throw new IllegalStateException(s"Cannot move from ${currentState} to ${nextState}")
    currentState = nextState.name
    gameStateByName(currentState).activity(stopWatch)
  }

  def gameStateByName(stateName: String): GameState = stateName match {
    case NotStarted.name => NotStarted
    case Running.name => Running
    case Paused.name => Paused
    case Cancelled.name => Cancelled
    case Won.name => Won
    case Lost.name => Lost
  }
}


