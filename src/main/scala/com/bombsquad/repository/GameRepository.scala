package com.bombsquad.repository

import com.bombsquad.model.{Game, GameList, GameRequest}
import org.mongodb.scala.bson.ObjectId

import scala.concurrent.Future

trait GameRepository {

  def createGame(username: String, gameRequest: GameRequest): Future[Game]

  def findGameById(gameId: ObjectId): Future[Game]

  def updateGame(game: Game): Future[Game]

  def findGameIdsByUsername(username: String): Future[GameList]
}
