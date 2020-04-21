package com.bombsquad.repository

import com.bombsquad.model.User

import scala.concurrent.Future

trait UserRepository {
  def createUser(user: User): Future[User]

  def findUserByUsername(username: String): Future[User]

  def findUserByUsernameAndPassword(username: String, password: String): Future[User]
}
