package com.bombsquad.model

import com.bombsquad.BaseUnitTest

class UserSpec extends BaseUnitTest {

  "User creation" should "accept a valid password" in {
    User("username", "1aA###")
    true should be (true)
  }

  "User creation" should "fail with an invalid password" in {
    an[IllegalArgumentException] should be thrownBy {
      User("username", "111111aA")
    }
  }

}
