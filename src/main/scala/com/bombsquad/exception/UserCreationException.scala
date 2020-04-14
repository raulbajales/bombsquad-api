package com.bombsquad.exception

case class UserCreationException(message: String) extends RuntimeException {
  override def getMessage: String = s"Cannot create user, error: $message"
}
