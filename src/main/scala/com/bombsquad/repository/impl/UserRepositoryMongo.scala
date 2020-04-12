package com.bombsquad.repository.impl

import com.bombsquad.model.User
import com.bombsquad.repository.UserRepository
import com.sfxcode.nosql.mongo.MongoDAO
import com.sfxcode.nosql.mongo.database.DatabaseProvider
import org.mongodb.scala.model.Filters._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait UserRepositoryMongo extends UserRepository with MongoCodecRegistry {

  override def createUser(user: User): Future[User] = {
    require(user != null, "user is required")
    UserMongoDao.insertOne(user).head().map(_ => user)
  }

  override def findUserByUserame(username: String): Future[User] = {
    require(username != null && !username.isBlank, "username is required")
    UserMongoDao.find(equal("username", username)).head()
  }

  object UserMongoDao extends MongoDAO[User](DatabaseProvider("mongodb", codecRegistry), "users")
}
