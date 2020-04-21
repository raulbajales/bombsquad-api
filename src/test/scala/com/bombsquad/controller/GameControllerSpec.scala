package com.bombsquad.controller

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, MessageEntity, StatusCodes}
import akka.http.scaladsl.testkit.RouteTestTimeout
import com.bombsquad.BaseControllerTest
import com.bombsquad.JsonFormats._
import com.bombsquad.model._
import com.bombsquad.service.{GameActor, GameService}

import scala.concurrent.duration._

class GameControllerSpec extends BaseControllerTest {

  before {
    ctxt = new StubsContext()
  }

  object TestGameActor
    extends GameActor
      with GameService
      with UserRepositoryStub
      with GameRepositoryStub

  lazy val testKit = ActorTestKit()

  implicit def typedSystem: ActorSystem[Nothing] = testKit.system

  implicit def default(implicit system: ActorSystem[Nothing]) = RouteTestTimeout(5 seconds)

  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.toClassic

  val gameActor = testKit.spawn(TestGameActor.behavior)
  val testController = new GameController(gameActor) with JwtSupport {
    val JWT_TEST_TOKEN: String = "test-token"
    val JWT_TEST_HEADER = RawHeader(JWT_ACCESS_TOKEN_HEADER_NAME, JWT_TEST_TOKEN)

    override def newJwtToken(username: String): String = JWT_TEST_TOKEN

    override def checkJwtToken(token: String, username: String): Unit = {}
  }
  val routes = testController.routes

  "be able to signup a new user" in {
    val request = Post("/bombsquad/users").withEntity(Marshal(ctxt.user).to[MessageEntity].futureValue)
    request ~> routes ~> check {
      status should ===(StatusCodes.Created)
      contentType should ===(ContentTypes.`application/json`)
      entityAs[User] should be(ctxt.user)
    }
  }

  "be able to login a user" in {
    val loginUserRequest = LoginUserRequest(ctxt.user.username, ctxt.user.password)
    val request = Post("/bombsquad/users/login").withEntity(Marshal(loginUserRequest).to[MessageEntity].futureValue)
    request ~> routes ~> check {
      header(testController.JWT_ACCESS_TOKEN_HEADER_NAME).map(
        _.value() should ===(testController.newJwtToken(ctxt.user.username))
      ).orElse(fail())
      status should ===(StatusCodes.OK)
    }
  }

  "be able to start a new game" in {
    val game = ctxt.game
    val gameRequest = GameRequest(game.board.rows, game.board.cols, 1)
    val request = Post(s"/bombsquad/users/${ctxt.user.username}/games")
      .withHeaders(testController.JWT_TEST_HEADER)
      .withEntity(Marshal(gameRequest).to[MessageEntity].futureValue)
    request ~> routes ~> check {
      status should ===(StatusCodes.Created)
      contentType should ===(ContentTypes.`text/plain(UTF-8)`)
      entityAs[String] should be(game._id.toString)
    }
  }

  "be able to list games for a user" in {
    val request = Get(s"/bombsquad/users/${ctxt.user.username}/games")
      .withHeaders(testController.JWT_TEST_HEADER)
    request ~> routes ~> check {
      status should ===(StatusCodes.OK)
      contentType should ===(ContentTypes.`application/json`)
      entityAs[GameList] should be(ctxt.gameList)
    }
  }

  "be able to get game state" in {
    val gameId = ctxt.game._id.toString
    val request = Get(s"/bombsquad/users/${ctxt.user.username}/games/$gameId")
      .withHeaders(testController.JWT_TEST_HEADER)
    request ~> routes ~> check {
      status should ===(StatusCodes.OK)
      contentType should ===(ContentTypes.`application/json`)
      entityAs[String].contains(gameId) should be(true)
      entityAs[String].contains("board") should be(true)
      entityAs[String].contains("username") should be(true)
      entityAs[String].contains("workflow") should be(true)
    }
  }

  "be able to pause a running game" in {
    ctxt.game.start()
    val gameId = ctxt.game._id.toString
    val request = Put(s"/bombsquad/users/${ctxt.user.username}/games/$gameId/pause")
      .withHeaders(testController.JWT_TEST_HEADER)
    request ~> routes ~> check {
      status should ===(StatusCodes.OK)
      contentType should ===(ContentTypes.`text/plain(UTF-8)`)
      entityAs[String] should be(gameId)
      ctxt.game.workflow.currentState should be(Paused.name)
    }
  }

  "be able to cancel a running game" in {
    ctxt.game.start()
    val gameId = ctxt.game._id.toString
    val request = Put(s"/bombsquad/users/${ctxt.user.username}/games/$gameId/cancel")
      .withHeaders(testController.JWT_TEST_HEADER)
    request ~> routes ~> check {
      status should ===(StatusCodes.OK)
      contentType should ===(ContentTypes.`text/plain(UTF-8)`)
      entityAs[String] should be(gameId)
      ctxt.game.workflow.currentState should be(Cancelled.name)
    }
  }

  "be able to flag a cell in a running game" in {
    ctxt.game.start()
    val gameId = ctxt.game._id.toString
    val request = Put(s"/bombsquad/users/${ctxt.user.username}/games/$gameId/flag?row=3&col=3")
      .withHeaders(testController.JWT_TEST_HEADER)
    request ~> routes ~> check {
      status should ===(StatusCodes.OK)
      contentType should ===(ContentTypes.`text/plain(UTF-8)`)
      entityAs[String] should be(gameId)
      ctxt.game.board.cellAt(3, 3).map(_.flagged should be(true))
    }
  }

  "be able to uncover a cell in a running game" in {
    ctxt.game.start()
    val gameId = ctxt.game._id.toString
    val request = Put(s"/bombsquad/users/${ctxt.user.username}/games/$gameId/uncover?row=3&col=3")
      .withHeaders(testController.JWT_TEST_HEADER)
    request ~> routes ~> check {
      status should ===(StatusCodes.OK)
      contentType should ===(ContentTypes.`text/plain(UTF-8)`)
      entityAs[String] should be(gameId)
      ctxt.game.board.cellAt(3, 3).map(_.covered should be(false))
    }
  }
}
