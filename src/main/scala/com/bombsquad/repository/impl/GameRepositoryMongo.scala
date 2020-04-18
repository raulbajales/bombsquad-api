package com.bombsquad.repository.impl

import com.bombsquad.model.{BoardFactory, Game, GameList, GameRequest}
import com.bombsquad.repository.GameRepository
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.ReplaceOptions

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait GameRepositoryMongo extends GameRepository with MongoSupport {

  lazy val gameColl: MongoCollection[Game] = database.getCollection("games")

  override def createGame(username: String, gameRequest: GameRequest): Future[Game] = {
    require(username != null && !username.isBlank, "username is required")
    val game = Game(username = username, board = BoardFactory.createWithRandomlyBuriedBombs(gameRequest))
    gameColl.insertOne(game).toFuture().map(_ => game)
  }

  override def findGameById(gameId: ObjectId): Future[Game] = {
    require(gameId != null, "gameId is required")
    gameColl.find(equal("_id", gameId)).head()
  }

  override def updateGame(game: Game): Future[Game] = {
    require(game != null, "game is required")
    gameColl.replaceOne(equal("_id", game._id), game, ReplaceOptions()).toFuture().map(_ => game)
  }

  override def findGameIdsByUsername(username: String): Future[GameList] = {
    require(username != null && !username.isBlank, "username is required")
    gameColl.find(
      equal("username", username)
    ).map(_._id.toString).toFuture().map(gameIds => GameList(gameIds.toList))
  }
}
