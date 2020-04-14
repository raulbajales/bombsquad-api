package com.bombsquad.controller

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.{BadRequest, Forbidden, InternalServerError}
import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.ExceptionHandler
import com.bombsquad.exception.{GameDoesNotBelongToUserException, UserCreationException}
import com.typesafe.scalalogging.LazyLogging

object CustomExceptionHandler extends LazyLogging {

  def handler: ExceptionHandler =
    ExceptionHandler {
      case e: GameDoesNotBelongToUserException =>
        extractUri { uri =>
          logger.info(s"Exception in request to $uri: ${e.getMessage}")
          complete(HttpResponse(Forbidden))
        }
      case e: IllegalArgumentException => {
        extractUri { uri =>
          logger.info(s"Exception in request to $uri: ${e.getMessage}")
          complete(HttpResponse(BadRequest))
        }
      }
      case e: IllegalStateException => {
        extractUri { uri =>
          logger.info(s"Exception in request to $uri: ${e.getMessage}")
          complete(HttpResponse(BadRequest))
        }
      }
      case e: UserCreationException => {
        extractUri { uri =>
          logger.info(s"Exception in request to $uri: ${e.getMessage}")
          complete(HttpResponse(BadRequest))
        }
      }
      case e: Throwable => {
        extractUri { uri =>
          logger.info(s"Exception in request to $uri: ${e.getMessage}")
          complete(HttpResponse(InternalServerError))
        }
      }
    }
}
