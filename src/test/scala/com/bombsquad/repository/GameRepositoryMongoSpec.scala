package com.bombsquad.repository

import com.bombsquad.repository.impl.GameRepositoryMongo
import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, Matchers}

class GameRepositoryMongoSpec
  extends AsyncFlatSpec
    with Matchers
    with MongoEmbedDatabase
    with BeforeAndAfter {
  var mongoProps: MongodProps = _

  object GameRepo extends GameRepositoryMongo

  before {
    try {
      mongoProps = mongoStart(27017)
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  after(mongoStop(mongoProps))

  "GameRepositoryMongo" should "be able to create a new game and retrieve it" in {
    val username = "testuser"
    val rows = 9
    val cols = 8
    val bombs = 15
    GameRepo.createGame(username, rows, cols, bombs).flatMap { game =>
      GameRepo.findGameById(game.id)
    }.map { game =>
      game should not be (null)
      game.username should be(username)
    }
  }
}
