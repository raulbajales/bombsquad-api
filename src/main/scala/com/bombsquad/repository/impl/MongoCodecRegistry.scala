package com.bombsquad.repository.impl

import com.bombsquad.model.{Board, Cell, Game, GameWorkflow, StopWatch, User}
import org.bson.codecs.configuration.CodecRegistries._
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY

trait MongoCodecRegistry {
  val codecRegistry: CodecRegistry =  fromRegistries(DEFAULT_CODEC_REGISTRY, fromProviders(
    classOf[User],
    classOf[Cell],
    classOf[StopWatch],
    classOf[Board],
    classOf[GameWorkflow],
    classOf[Game]
  ))
}
