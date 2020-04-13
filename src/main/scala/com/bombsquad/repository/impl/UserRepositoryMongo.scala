package com.bombsquad.repository.impl

import com.bombsquad.model.User
import com.bombsquad.repository.UserRepository
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.model.Filters._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait UserRepositoryMongo extends UserRepository with MongoSupport {

  lazy val userColl: MongoCollection[User] = database.getCollection("users")

  override def createUser(user: User): Future[User] = {
    require(user != null, "user is required")
    userColl.insertOne(user).toFuture().map(_ => user)
  }

  override def findUserByUserame(username: String): Future[User] = {
    require(username != null && !username.isBlank, "username is required")
    userColl.find(equal("username", username)).head()
  }
}
