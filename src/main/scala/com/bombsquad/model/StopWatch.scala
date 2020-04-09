package com.bombsquad.model

import java.time.Duration

case class StopWatch(var elapsed: Duration = Duration.ofMillis(0)) {
  private var startedAtMillis: Long = 0

  def start(): Unit = {
    startedAtMillis = System.currentTimeMillis()
  }

  def stop(): Unit = {
    elapsed = Duration.ofMillis((System.currentTimeMillis() - startedAtMillis) + elapsed.toMillis)
  }
}