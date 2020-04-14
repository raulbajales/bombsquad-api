package com.bombsquad

import java.time.Duration

import com.typesafe.config.{Config, ConfigFactory}

object AppConf {

  private lazy val conf: Config = ConfigFactory.load()

  //
  // Server configurations:
  //

  def routesAskTimeout: Duration = conf.getDuration("bombsquad-server.routes.ask-timeout")
  def host: String = conf.getString("bombsquad-server.host")

  //
  // MongoDB configurations:
  //

  def mongoDatabase: String = conf.getString("mongodb.database")

  def mongoURI: String = conf.getString("mongodb.uri")

  //
  // Game specific configurations:
  //

  def defaultRows: Int = conf.getInt("bombsquad.default-rows")

  def defaultCols: Int = conf.getInt("bombsquad.default-cols")

  def defaultBombs: Int = conf.getInt("bombsquad.default-bombs")

}
