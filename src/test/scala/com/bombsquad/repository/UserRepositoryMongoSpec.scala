package com.bombsquad.repository

import com.bombsquad.model.User
import com.bombsquad.repository.impl.UserRepositoryMongo
import com.github.simplyscala.{MongoEmbedDatabase, MongodProps}
import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, Matchers}

class UserRepositoryMongoSpec
  extends AsyncFlatSpec
    with Matchers
    with MongoEmbedDatabase
    with BeforeAndAfter {
  var mongoProps: MongodProps = _

  object UserRepo extends UserRepositoryMongo

  before {
    try {
      mongoProps = mongoStart(27017)
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  after(mongoStop(mongoProps))

  "UserRepositoryMongo" should "be able to create a new user and retrieve it" in {
    val username = "testuser"
    UserRepo.createUser(User(username)).flatMap { _ =>
      UserRepo.findUserByUserame(username)
    }.map { user =>
      user should not be (null)
      user.username should be(username)
    }
  }
}
