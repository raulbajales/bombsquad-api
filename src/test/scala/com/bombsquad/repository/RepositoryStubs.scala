package com.bombsquad.repository

import com.bombsquad.model.{BoardFactory, Game, GameList, GameRequest, User}
import org.mongodb.scala.bson.ObjectId

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RepositoryStubs {

  var ctxt: StubsContext = null

  class StubsContext {
    var calls = mutable.Queue[(String, Any)]()
    var gameIdObj = new ObjectId()
    var username = "johndoe"
    var user = User(username)
    var game = Game(_id = gameIdObj, username = username, board = BoardFactory.createWithSpecificBuriedBombs(5, 5, Set((4, 4))))
    var gameList = GameList(List(gameIdObj.toString))
  }

  trait UserRepositoryStub extends UserRepository {
    def createUser(user: User): Future[User] = {
      ctxt.calls.enqueue(("createUser", user))
      Future(user)
    }

    def findUserByUsername(username: String): Future[User] = {
      val retValue = ctxt.user
      ctxt.calls.enqueue(("findUserByUsername", retValue))
      Future(retValue)
    }
  }

  trait GameRepositoryStub extends GameRepository {
    def createGame(username: String, gameRequest: GameRequest): Future[Game] = {
      val retValue = ctxt.game
      ctxt.calls.enqueue(("createGame", retValue))
      Future(retValue)
    }

    def findGameById(gameId: ObjectId): Future[Game] = {
      ctxt.calls.enqueue(("findGameById", ctxt.game))
      Future(ctxt.game)
    }

    def updateGame(game: Game): Future[Game] = {
      ctxt.calls.enqueue(("updateGame", game))
      Future(game)
    }

    def findGameIdsByUsername(username: String): Future[GameList] = {
      val retValue = ctxt.gameList
      ctxt.calls.enqueue(("findGameIdsByUsername", retValue))
      Future(retValue)
    }
  }

}
