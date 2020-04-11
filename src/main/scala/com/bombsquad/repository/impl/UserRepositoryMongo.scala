package com.bombsquad.repository.impl

import com.bombsquad.model.User
import com.bombsquad.repository.UserRepository
import com.sfxcode.nosql.mongo.MongoDAO
import com.sfxcode.nosql.mongo.database.DatabaseProvider
import org.bson.codecs.configuration.CodecRegistries.fromProviders
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait UserRepositoryMongo extends UserRepository {

  object delegate extends MongoDAO[User](DatabaseProvider("mongodb", fromProviders(classOf[User])), "users")

  override def createUser(user: User): Future[User] = {
    require(user != null, "user is required")
    delegate.insertOne(user)
    Future(user)
  }

  override def findUserByUserame(username: String): Future[User] = {
    require(username != null && username.isBlank, "username is required")
    delegate.find(equal("username", username)).head()
  }
}
