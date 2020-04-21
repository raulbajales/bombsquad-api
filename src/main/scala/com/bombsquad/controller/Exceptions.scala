package com.bombsquad.controller

case class UserLoginException(message: String) extends RuntimeException {
  override def getMessage: String = s"Cannot login user, error: $message"
}

case class UserCreationException(message: String) extends RuntimeException {
  override def getMessage: String = s"Cannot create user, error: $message"
}

final case class GameDoesNotBelongToUserException(username: String, gameId: String) extends RuntimeException {
  override def getMessage: String = s"User $username does not have access to game $gameId"
}

final case class JwtTokenException() extends RuntimeException {
  override def getMessage: String = s"Error validating security token"
}