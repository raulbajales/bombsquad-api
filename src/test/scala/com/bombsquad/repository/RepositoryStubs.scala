package com.bombsquad.repository

import com.bombsquad.model._
import org.mongodb.scala.bson.ObjectId

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RepositoryStubs {

  var ctxt: StubsContext = null

  trait UserRepositoryStub extends UserRepository {
    override def createUser(user: User): Future[User] = {
      ctxt.calls.enqueue(("createUser", user))
      Future(user)
    }

    override def findUserByUsername(username: String): Future[User] = {
      val retValue = ctxt.user
      ctxt.calls.enqueue(("findUserByUsername", retValue))
      Future(retValue)
    }

    override def findUserByUsernameAndPassword(username: String, password: String): Future[User] = {
      val retValue = ctxt.user
      ctxt.calls.enqueue(("findUserByUsernameAndPassword", retValue))
      Future(retValue)
    }
  }

  trait GameRepositoryStub extends GameRepository {
    override def createGame(username: String, gameRequest: GameRequest): Future[Game] = {
      val retValue = ctxt.game
      ctxt.calls.enqueue(("createGame", retValue))
      Future(retValue)
    }

    override def findGameById(gameId: ObjectId): Future[Game] = {
      ctxt.calls.enqueue(("findGameById", ctxt.game))
      Future(ctxt.game)
    }

    override def updateGame(game: Game): Future[Game] = {
      ctxt.calls.enqueue(("updateGame", game))
      Future(game)
    }

    override def findGameIdsByUsername(username: String): Future[GameList] = {
      val retValue = ctxt.gameList
      ctxt.calls.enqueue(("findGameIdsByUsername", retValue))
      Future(retValue)
    }
  }

  class StubsContext {
    var calls = mutable.Queue[(String, Any)]()
    var gameIdObj = new ObjectId()
    var username = "johndoe"
    var user = User(username, "aA1###")
    var game = Game(_id = gameIdObj, username = username, board = BoardFactory.createWithSpecificBuriedBombs(5, 5, Set((4, 4))))
    var gameList = GameList(List(gameIdObj.toString))
  }

}
