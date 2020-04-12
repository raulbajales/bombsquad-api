package com.bombsquad.model

import java.time.Duration

case class StopWatch(var elapsedInSeconds: Long = 0) {
  private var startedAtMillis: Long = 0

  def start(): Unit = {
    startedAtMillis = System.currentTimeMillis()
  }

  def stop(): Unit = {
    elapsedInSeconds = Duration.ofMillis((System.currentTimeMillis() - startedAtMillis) + Duration.ofSeconds(elapsedInSeconds).toMillis).toSeconds
  }
}