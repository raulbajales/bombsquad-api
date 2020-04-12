package com.bombsquad.repository

import com.bombsquad.model.Game

import scala.concurrent.Future

trait GameRepository {

  def createGame(username: String, rows: Int, cols: Int, bombs: Int): Future[Game]

  def findGameById(gameId: String): Future[Game]

  def updateGame(game: Game): Future[Game]

  def findGameIdsByUsername(username: String): Future[Seq[String]]
}
