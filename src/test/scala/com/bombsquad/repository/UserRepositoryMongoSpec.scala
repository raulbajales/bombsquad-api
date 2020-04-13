package com.bombsquad.repository

import com.bombsquad.model.User
import com.bombsquad.repository.impl.UserRepositoryMongo
import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, Matchers}

class UserRepositoryMongoSpec
  extends AsyncFlatSpec
    with Matchers
    with BeforeAndAfter {

  object UserRepo extends UserRepositoryMongo

  before {
    UserRepo.userColl.drop()
  }

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
