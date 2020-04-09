package com.bombsquad.model

sealed trait GameState {
  val canMoveTo: Array[GameState] = Array()
  def activity(stopWatch: StopWatch): Unit = {}
}

sealed trait GameStoppedState extends GameState

case object NotStarted extends GameState {
  override val canMoveTo: Array[GameState] = Array(Running)
}

case object Running extends GameState {
  override val canMoveTo: Array[GameState] = Array(Paused, Cancelled, Won, Lost)
  override def activity(stopWatch: StopWatch): Unit = stopWatch.start()
}

case object Paused extends GameState {
  override val canMoveTo: Array[GameState] = Array(Running, Cancelled)
  override def activity(stopWatch: StopWatch): Unit = stopWatch.stop()
}

case object Cancelled extends GameStoppedState {
  override def activity(stopWatch: StopWatch): Unit = stopWatch.stop()
}

case object Won extends GameStoppedState {
  override def activity(stopWatch: StopWatch): Unit = stopWatch.stop()
}

case object Lost extends GameStoppedState {
  override def activity(stopWatch: StopWatch): Unit = stopWatch.stop()
}

case class GameWorkflow(var currentState: GameState = NotStarted, stopWatch: StopWatch = StopWatch()) {
  def moveTo(nextState: GameState): Unit = {
    if (!currentState.canMoveTo.contains(nextState))
      throw new IllegalStateException(s"Cannot move from ${currentState} to ${nextState}")
    currentState = nextState
    currentState.activity(stopWatch)
  }
}


