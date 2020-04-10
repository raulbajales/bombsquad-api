package com.bombsquad.model

case class User(username: String) {
  require(username != null && !username.isEmpty, "id is needed")
}

object User {
  def guest(): User = User(s"GUEST_USER-${System.currentTimeMillis()}")
}
