package com.bombsquad.repository

import com.bombsquad.BaseUnitTest
import com.bombsquad.model.User
import com.bombsquad.repository.impl.UserRepositoryMongo

import scala.concurrent.Await
import scala.concurrent.duration._

class UserRepositoryMongoSpec extends BaseUnitTest {

  object UserRepo extends UserRepositoryMongo

  before {
    Await.result(UserRepo.userColl.drop().toFuture(), 3 seconds)
  }

  "UserRepositoryMongo" should "be able to create a new user and retrieve it" in {
    val username = "testuser"
    UserRepo.createUser(User(username)).flatMap { _ =>
      UserRepo.findUserByUsername(username)
    }.map { user =>
      user should not be (null)
      user.username should be(username)
    }
  }
}
