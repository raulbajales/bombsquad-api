package com.bombsquad

import com.bombsquad.model._
import org.mongodb.scala.bson.ObjectId
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, RootJsonFormat}

object JsonFormats {

  implicit object GameStateFormat extends JsonFormat[GameState] {
    def write(gameState: GameState): JsString =
      JsString(gameState.name)

    def read(value: JsValue): GameState = value match {
      case JsString(value) => value match {
        case NotStarted.name => NotStarted
        case Running.name => Running
        case Paused.name => Paused
        case Cancelled.name => Cancelled
        case Won.name => Won
        case Lost.name => Lost
      }
      case _ => throw new RuntimeException("GameState expected")
    }
  }

  implicit object ObjectIdFormat extends RootJsonFormat[ObjectId] {
    override def write(obj: ObjectId): JsValue = JsString(obj.toString)

    override def read(json: JsValue): ObjectId = new ObjectId(json.toString())
  }

  import DefaultJsonProtocol._

  implicit val gameRequestJsonFormat: RootJsonFormat[GameRequest] = jsonFormat3(GameRequest)
  implicit val stopWatchJsonFormat: RootJsonFormat[StopWatch] = jsonFormat1(StopWatch)
  implicit val cellJsonFormat: RootJsonFormat[Cell] = jsonFormat4(Cell)
  implicit val gameWorkflowJsonFormat: RootJsonFormat[GameWorkflow] = jsonFormat2(GameWorkflow)
  implicit val boardJsonFormat: RootJsonFormat[Board] = jsonFormat1(Board)
  implicit val gameListJsonFormat: RootJsonFormat[GameList] = jsonFormat1(GameList)
  implicit val gameJsonFormat: RootJsonFormat[Game] = jsonFormat4(Game)
  implicit val userJsonFormat: RootJsonFormat[User] = jsonFormat1(User)
}
