package com.bombsquad.repository.impl

import com.bombsquad.model.{BoardFactory, Game}
import com.bombsquad.repository.GameRepository
import com.sfxcode.nosql.mongo.MongoDAO
import com.sfxcode.nosql.mongo.database.DatabaseProvider
import org.bson.types.ObjectId
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait GameRepositoryMongo extends GameRepository with MongoCodecRegistry {

  override def createGame(username: String, rows: Int, cols: Int, bombs: Int): Future[Game] = {
    require(username != null && !username.isBlank, "username is required")
    val game = Game(id = ObjectId.get().toString, username = username, board = BoardFactory.createWithRandomlyBuriedBombs(rows, cols, bombs))
    GameMongoDao.insertOne(game).toFuture().map(_ => game)
  }

  override def findGameById(gameId: String): Future[Game] = {
    require(gameId != null && !gameId.isBlank, "gameId is required")
    GameMongoDao.findById(new ObjectId(gameId)).head()
  }

  override def updateGame(game: Game): Future[Game] = {
    require(game != null, "game is required")
    GameMongoDao.replaceOne(game).toFuture().map(_ => game)
  }

  override def findGameIdsByUsername(username: String): Future[Seq[String]] = {
    require(username != null && !username.isBlank, "username is required")
    GameMongoDao.find(
      equal("username", username)
    ).projection(
      fields(include("_id"))
    ).map(_.id).toFuture()
  }

  object GameMongoDao extends MongoDAO[Game](DatabaseProvider("mongodb", codecRegistry), "games")
}
