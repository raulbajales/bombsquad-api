package com.bombsquad.repository

import com.bombsquad.BaseUnitTest
import com.bombsquad.model.{GameRequest, NotStarted, Won}
import com.bombsquad.repository.impl.GameRepositoryMongo

import scala.concurrent.Await
import scala.concurrent.duration._

class GameRepositoryMongoSpec extends BaseUnitTest {

  before {
    Await.result(GameRepo.gameColl.drop().toFuture(), 3 seconds)
  }

  object GameRepo extends GameRepositoryMongo

  "GameRepositoryMongo" should "be able to create a new game and retrieve it" in {
    val username = "testuser"
    val rows = 9
    val cols = 8
    GameRepo.createGame(username, GameRequest(rows, cols, 15)).flatMap { createdGame =>
      GameRepo.findGameById(createdGame._id)
    }.map { game =>
      game should not be (null)
      game.username should be(username)
      game.board.rows should equal(rows)
      game.board.cols should equal(cols)
      game.workflow.currentState should be(NotStarted.name)
      game.workflow.stopWatch.elapsedInSeconds should be(0)
    }
  }

  "GameRepositoryMongo" should "be able to update game state" in {
    val username = "testuser"
    val rows = 9
    val cols = 8
    GameRepo.createGame(username, GameRequest(rows, cols, 0)).flatMap { game =>
      GameRepo.findGameById(game._id).flatMap { retrievedGame =>
        retrievedGame.start()
        Thread.sleep(1500)
        retrievedGame.unCoverCell(3, 4)
        GameRepo.updateGame(retrievedGame)
      }
    }.map { game =>
      game should not be (null)
      game.username should be(username)
      game.board.rows should equal(rows)
      game.board.cols should equal(cols)
      game.workflow.currentState should be(Won.name)
      game.workflow.stopWatch.elapsedInSeconds should be(1)
      game.board.cellAt(3, 4).get.covered should be(false)
    }
  }

  "GameRepositoryMongo" should "be able to get the list of game ids for a given username" in {
    val username1 = "testuser1"
    val username2 = "testuser2"
    for {
      _ <- GameRepo.createGame(username1, GameRequest(5, 5, 5))
      gameForUser2 <- GameRepo.createGame(username2, GameRequest(6, 6, 5))
      gameList <- GameRepo.findGameIdsByUsername(username2)
    } yield {
      gameList should not be (null)
      gameList.gameIds.size should be(1)
      gameList.gameIds.contains(gameForUser2._id.toString) should be(true)
    }
  }
}
