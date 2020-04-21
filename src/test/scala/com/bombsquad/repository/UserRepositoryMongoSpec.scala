package com.bombsquad.repository

import com.bombsquad.BaseUnitTestAsync
import com.bombsquad.model.User
import com.bombsquad.repository.impl.UserRepositoryMongo

import scala.concurrent.Await
import scala.concurrent.duration._

class UserRepositoryMongoSpec extends BaseUnitTestAsync {

  object UserRepo extends UserRepositoryMongo

  before {
    Await.result(UserRepo.userColl.drop().toFuture(), 3 seconds)
  }

  "UserRepositoryMongo" should "be able to create a new user and retrieve it by username" in {
    val username = "testuser"
    UserRepo.createUser(User(username, "aA1###")).flatMap { _ =>
      UserRepo.findUserByUsername(username)
    }.map { user =>
      user should not be (null)
      user.username should be(username)
    }
  }

  "UserRepositoryMongo" should "be able to create a new user and retrieve it by username and password" in {
    val username = "testuser"
    val password = "aA1###"
    UserRepo.createUser(User(username, password)).flatMap { _ =>
      UserRepo.findUserByUsernameAndPassword(username, password)
    }.map { user =>
      user should not be (null)
      user.username should be(username)
    }
  }
}
