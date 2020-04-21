package com.bombsquad.controller

import com.bombsquad.BaseUnitTest

class JwtSupportSpec extends BaseUnitTest {

  "JwtSupport" should "be able to validate a token checking the username" in {
    object TestJwtSupport extends JwtSupport
    val username = "username"
    TestJwtSupport.checkJwtToken(TestJwtSupport.newJwtToken(username), username)
    true should be (true)
  }

  "JwtSupport" should "be able fail validation for a token that belongs to another username" in {
    object TestJwtSupport extends JwtSupport
    val username = "username"
    an[JwtTokenException] should be thrownBy {
      TestJwtSupport.checkJwtToken(TestJwtSupport.newJwtToken("someUsername"), "anotherUsername")
    }
  }

  "JwtSupport" should "be able fail validation for an expired token" in {
    object TestJwtSupport extends JwtSupport {
      override def jwtTokenLifeExpectancyInSeconds: Long = 0
    }
    val username = "username"
    an[JwtTokenException] should be thrownBy {
      TestJwtSupport.checkJwtToken(TestJwtSupport.newJwtToken(username), username)
    }
  }

}
