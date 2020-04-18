package com.bombsquad.service

import com.bombsquad.model._
import com.bombsquad.repository.RepositoryStubs
import org.mongodb.scala.bson.ObjectId
import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, Matchers}

class GameServiceSpec extends AsyncFlatSpec with Matchers with BeforeAndAfter with RepositoryStubs {

  before {
    ctxt = new StubsContext()
  }

  object TestGameService extends GameService with UserRepositoryStub with GameRepositoryStub

  "GameService" should "be able to create a user" in {
    TestGameService.createUser(ctxt.user).map { user =>
      ctxt.calls.dequeue() should be(("createUser", user))
      user should not be (null)
      user.username should be(ctxt.username)
    }
  }

  "GameService" should "be able to start a new game and store it" in {
    TestGameService.startNewGame(ctxt.username, GameRequest(5, 5, 0)).map { gameId =>
      ctxt.calls.dequeue() should be(("findUserByUsername", User(ctxt.username)))
      ctxt.calls.dequeue() should be(("createGame", ctxt.game))
      ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

      gameId should be(ctxt.gameIdObj.toString)
    }
  }

  "GameService" should "be able to pause a running game and store it" in {
    TestGameService.startNewGame(ctxt.username, GameRequest(5, 5, 0)).flatMap { gameId =>
      ctxt.calls.dequeue() should be(("findUserByUsername", User(ctxt.username)))
      ctxt.calls.dequeue() should be(("createGame", ctxt.game))
      ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

      TestGameService.pauseGame(ctxt.username, new ObjectId(gameId)).flatMap { gameId =>
        ctxt.calls.dequeue() should be(("findGameIdsByUsername", ctxt.gameList))
        ctxt.calls.dequeue() should be(("findGameById", ctxt.game))
        ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

        ctxt.game.workflow.currentState should be(Paused.name)
        gameId should be(ctxt.gameIdObj.toString)
      }
    }
  }

  "GameService" should "be able to cancel a running game and store it" in {
    TestGameService.startNewGame(ctxt.username, GameRequest(5, 5, 0)).flatMap { gameId =>
      ctxt.calls.dequeue() should be(("findUserByUsername", User(ctxt.username)))
      ctxt.calls.dequeue() should be(("createGame", ctxt.game))
      ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

      TestGameService.cancelGame(ctxt.username, new ObjectId(gameId)).flatMap { gameId =>
        ctxt.calls.dequeue() should be(("findGameIdsByUsername", ctxt.gameList))
        ctxt.calls.dequeue() should be(("findGameById", ctxt.game))
        ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

        ctxt.game.workflow.currentState should be(Cancelled.name)
        gameId should be(ctxt.gameIdObj.toString)
      }
    }
  }

  "GameService" should "be able to flag a cell in a running game and store it" in {
    TestGameService.startNewGame(ctxt.username, GameRequest(5, 5, 0)).flatMap { gameId =>
      ctxt.calls.dequeue() should be(("findUserByUsername", User(ctxt.username)))
      ctxt.calls.dequeue() should be(("createGame", ctxt.game))
      ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

      TestGameService.flagCell(ctxt.username, new ObjectId(gameId), 3, 3).flatMap { gameId =>
        ctxt.calls.dequeue() should be(("findGameIdsByUsername", ctxt.gameList))
        ctxt.calls.dequeue() should be(("findGameById", ctxt.game))
        ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

        ctxt.game.workflow.currentState should be(Running.name)
        ctxt.game.board.cellAt(3, 3) match {
          case Some(cell) => cell.flagged should be(true)
        }
        gameId should be(ctxt.gameIdObj.toString)
      }
    }
  }

  "GameService" should "be able to uncover a cell in a running game and store it" in {
    TestGameService.startNewGame(ctxt.username, GameRequest(5, 5, 0)).flatMap { gameId =>
      ctxt.calls.dequeue() should be(("findUserByUsername", User(ctxt.username)))
      ctxt.calls.dequeue() should be(("createGame", ctxt.game))
      ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

      TestGameService.unCoverCell(ctxt.username, new ObjectId(gameId), 3, 3).flatMap { gameId =>
        ctxt.calls.dequeue() should be(("findGameIdsByUsername", ctxt.gameList))
        ctxt.calls.dequeue() should be(("findGameById", ctxt.game))
        ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

        ctxt.game.workflow.currentState should be(Running.name)
        ctxt.game.board.cellAt(3, 3) match {
          case Some(cell) => cell.covered should be(false)
        }
        gameId should be(ctxt.gameIdObj.toString)
      }
    }
  }

  "GameService" should "be able to get game state in a running game and store it" in {
    TestGameService.startNewGame(ctxt.username, GameRequest(5, 5, 0)).flatMap { gameId =>
      ctxt.calls.dequeue() should be(("findUserByUsername", User(ctxt.username)))
      ctxt.calls.dequeue() should be(("createGame", ctxt.game))
      ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

      TestGameService.unCoverCell(ctxt.username, new ObjectId(gameId), 3, 3).flatMap { gameId =>
        ctxt.calls.dequeue() should be(("findGameIdsByUsername", ctxt.gameList))
        ctxt.calls.dequeue() should be(("findGameById", ctxt.game))
        ctxt.calls.dequeue() should be(("updateGame", ctxt.game))

        ctxt.game.workflow.currentState should be(Running.name)
        ctxt.game.board.cellAt(3, 3) match {
          case Some(cell) => cell.covered should be(false)
        }
        gameId should be(ctxt.gameIdObj.toString)

        TestGameService.gameState(ctxt.username, new ObjectId(gameId)).flatMap { game =>
          ctxt.calls.dequeue() should be(("findGameIdsByUsername", ctxt.gameList))
          ctxt.calls.dequeue() should be(("findGameById", ctxt.game))

          game.workflow.currentState should be(Running.name)
          game.board.cellAt(3, 3) match {
            case Some(cell) => cell.covered should be(false)
          }
        }
      }
    }
  }

  "GameService" should "be able to get list og game ids for a user" in {
    TestGameService.listGamesFor(ctxt.username).flatMap { gameList =>
      ctxt.calls.dequeue() should be(("findGameIdsByUsername", ctxt.gameList))
      gameList should be(ctxt.gameList)
    }
  }
}
