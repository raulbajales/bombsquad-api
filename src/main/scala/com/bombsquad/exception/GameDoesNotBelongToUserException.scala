package com.bombsquad.exception

final case class GameDoesNotBelongToUserException(username: String, gameId: String) extends RuntimeException {
  override def getMessage: String = s"User $username does not have access to game $gameId"
}
