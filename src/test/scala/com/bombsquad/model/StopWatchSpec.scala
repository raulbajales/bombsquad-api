package com.bombsquad.model

import java.time.Duration

import org.scalatest.{FlatSpec, Matchers}

class StopWatchSpec extends FlatSpec with Matchers {

  "StopWatch" should "count elapsed time even with multiple pauses" in {
    val stopWatch = StopWatch()
    stopWatch.start()
    Thread.sleep(1000)
    stopWatch.stop()
    Thread.sleep(1000)
    stopWatch.start()
    Thread.sleep(1000)
    stopWatch.stop()
    stopWatch.elapsedInSeconds should be(Duration.ofSeconds(2).toSeconds)
  }

  "StopWatch" should "count elapsed time even with multiple pauses, considering a current elapsed time" in {
    val stopWatch = StopWatch(2)
    stopWatch.start()
    Thread.sleep(1000)
    stopWatch.stop()
    Thread.sleep(1000)
    stopWatch.start()
    Thread.sleep(1000)
    stopWatch.stop()
    stopWatch.elapsedInSeconds should be(Duration.ofSeconds(4).toSeconds)
  }
}