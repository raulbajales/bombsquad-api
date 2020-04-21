package com.bombsquad.controller

import java.time.Instant

import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}

import scala.util.{Failure, Success}

trait JwtSupport {
  private val jwtSecretKey: String = "30m5q@d"

  val JWT_ACCESS_TOKEN_HEADER_NAME: String = "Access-Token"

  def jwtTokenLifeExpectancyInSeconds: Long = Instant.now.plusSeconds(24 * 60 * 60).getEpochSecond

  def newJwtToken(username: String): String = {
    val claim = JwtClaim(subject = Some(username), expiration = Some(jwtTokenLifeExpectancyInSeconds), issuedAt = Some(Instant.now.getEpochSecond))
    JwtSprayJson.encode(claim, jwtSecretKey, JwtAlgorithm.HS256)
  }

  def checkJwtToken(token: String, username: String): Unit = JwtSprayJson.decode(token, jwtSecretKey, Seq(JwtAlgorithm.HS256)) match {
    case Failure(_) => throw JwtTokenException()
    case Success(claim) => claim.subject match {
      case Some(subject) if subject.equals(username) => claim.expiration.map { exp =>
        if (exp < Instant.now.getEpochSecond) throw JwtTokenException()
      }.orElse(throw JwtTokenException())
      case _ => throw JwtTokenException()
    }
  }
}
