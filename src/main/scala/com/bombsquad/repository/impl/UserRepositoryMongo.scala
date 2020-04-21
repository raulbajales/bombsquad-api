package com.bombsquad.repository.impl

import com.bombsquad.model.User
import com.bombsquad.repository.UserRepository
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.{IndexOptions, Indexes}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait UserRepositoryMongo extends UserRepository with MongoSupport {

  val userColl: MongoCollection[User] = database.getCollection("users")

  userColl.createIndex(
    Indexes.ascending("username"),
    IndexOptions().background(false).unique(true)
  ).toFuture()

  override def createUser(user: User): Future[User] = {
    require(user != null, "user is required")
    userColl.insertOne(user).toFuture().map(_ => user)
  }

  override def findUserByUsername(username: String): Future[User] = {
    require(username != null && !username.isBlank, "username is required")
    userColl.find(equal("username", username)).head()
  }

  override def findUserByUsernameAndPassword(username: String, password: String): Future[User] = {
    require(username != null && !username.isBlank, "username is required")
    require(password != null && !password.isBlank, "password is required")
    userColl.find(and(equal("username", username), equal("password", password))).head()
  }
}
