package com.bombsquad.repository

import com.bombsquad.model.{NotStarted, Won}
import com.bombsquad.repository.impl.GameRepositoryMongo
import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, Matchers}

class GameRepositoryMongoSpec
  extends AsyncFlatSpec
    with Matchers
    with BeforeAndAfter {

  before {
    GameRepo.gameColl.drop()
  }

  object GameRepo extends GameRepositoryMongo

  "GameRepositoryMongo" should "be able to create a new game and retrieve it" in {
    val username = "testuser"
    val rows = 9
    val cols = 8
    GameRepo.createGame(username, rows, cols, 15).flatMap { game =>
      GameRepo.findGameById(game._id)
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
    GameRepo.createGame(username, rows, cols, 0).flatMap { game =>
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
}
