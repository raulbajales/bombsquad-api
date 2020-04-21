package com.bombsquad.model

import java.time.Duration

import com.bombsquad.BaseUnitTest

class GameWorkflowSpec extends BaseUnitTest {

  "GameWorkflow" should "be be created with current state default to NotStarted" in {
    GameWorkflow().currentState should be(NotStarted.name)
  }

  "GameWorkflow" should "be able to move into Running if current state is NotStarted" in {
    GameWorkflow().moveTo(Running)
  }

  "GameWorkflow" should "be able to move into Paused, Cancelled, Won or Lost if current state Running" in {
    Array(Paused, Cancelled, Won, Lost).foreach { state =>
      val workflow = GameWorkflow()
      workflow.moveTo(Running)
      workflow.moveTo(state)
    }
  }

  "GameWorkflow" should "be able to move into Running or Cancelled if current state Paused" in {
    Array(Running, Cancelled).foreach { state =>
      val workflow = GameWorkflow()
      workflow.moveTo(Running)
      workflow.moveTo(Paused)
      workflow.moveTo(state)
    }
  }

  "GameWorkflow" should "keep track of time when last state is Won, Lost or Cancelled (even with pauses in the middle)" in {
    Array(Won, Lost, Cancelled).foreach { lastState =>
      val workflow = GameWorkflow()
      workflow.moveTo(Running)
      Thread.sleep(1000)
      workflow.moveTo(Paused)
      Thread.sleep(1000)
      workflow.moveTo(Running)
      Thread.sleep(1000)
      workflow.moveTo(lastState)
      workflow.stopWatch.elapsedInSeconds should be(Duration.ofSeconds(2).toSeconds)
    }
  }

  "GameWorkflow" should "keep track of time (with an existent elapsed time) when last state is Won, Lost or Cancelled (even with pauses in the middle)" in {
    Array(Won, Lost, Cancelled).foreach { lastState =>
      val workflow = GameWorkflow(stopWatch = StopWatch(2))
      workflow.moveTo(Running)
      Thread.sleep(1000)
      workflow.moveTo(Paused)
      Thread.sleep(1000)
      workflow.moveTo(Running)
      Thread.sleep(1000)
      workflow.moveTo(lastState)
      workflow.stopWatch.elapsedInSeconds should be(Duration.ofSeconds(4).toSeconds)
    }
  }
}
