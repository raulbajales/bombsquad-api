package com.bombsquad.repository.impl

import com.bombsquad.AppConf
import com.bombsquad.model._
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

trait MongoSupport {
  val database = MongoClient(AppConf.mongoURI)
    .getDatabase(AppConf.mongoDatabase)
    .withCodecRegistry(fromRegistries(DEFAULT_CODEC_REGISTRY, fromProviders(
      classOf[User],
      classOf[Cell],
      classOf[StopWatch],
      classOf[Board],
      classOf[GameWorkflow],
      classOf[Game])))
}
