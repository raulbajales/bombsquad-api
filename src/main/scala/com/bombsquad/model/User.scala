package com.bombsquad.model

import scala.util.matching.Regex

case class User(username: String, password: String) {
  @transient private val PASSWORD_PATTERN: Regex = "^(?=.*[\\w])(?=.*[\\W])[\\w\\W]{6,}$".r

  require(username != null && !username.isEmpty, "username is needed")
  require(password != null &&
    !password.isEmpty &&
    PASSWORD_PATTERN.findFirstMatchIn(password).isDefined,
    "Invalid password: At least one lowercase, at least one uppercase, at least one digit, at least one special character, at least it should have 6 characters long.")
}

case class LoginUserRequest(username: String, password: String) {
  require(username != null && !username.isEmpty, "username is needed")
  require(password != null && !password.isEmpty, "password is needed")
}