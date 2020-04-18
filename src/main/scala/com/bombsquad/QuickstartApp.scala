package com.bombsquad

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.bombsquad.controller.GameController
import com.bombsquad.repository.impl.{GameRepositoryMongo, UserRepositoryMongo}
import com.bombsquad.service.{GameActor, GameService}

import scala.util.{Failure, Success}

object DefaultGameActor
  extends GameActor
    with GameService
    with GameRepositoryMongo
    with UserRepositoryMongo

object QuickstartApp {
  def main(args: Array[String]): Unit = {
    val port: Int = sys.env.getOrElse("PORT", "8080").toInt
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val gameActor = context.spawn(DefaultGameActor.behavior, "GameActor")
      context.watch(gameActor)
      val controller = new GameController(gameActor)(context.system)
      startHttpServer(controller.routes, context.system, port)
      Behaviors.empty
    }
    ActorSystem[Nothing](rootBehavior, "BombsquadServer")
  }

  private def startHttpServer(routes: Route, system: ActorSystem[_], port: Int): Unit = {
    implicit val classicSystem: akka.actor.ActorSystem = system.toClassic
    import system.executionContext
    val futureBinding = Http().bindAndHandle(routes, AppConf.serverHost, port)
    futureBinding.onComplete {
      case Success(binding) =>
        system.log.info(s"Server online at http://${binding.localAddress.getHostString}:${binding.localAddress.getPort}/")
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }
}
