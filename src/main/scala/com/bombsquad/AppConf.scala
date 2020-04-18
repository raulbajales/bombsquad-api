package com.bombsquad

import java.time.Duration

import com.typesafe.config.{Config, ConfigFactory}

object AppConf {

  var configLoader: Config = ConfigFactory.load()

  private lazy val configObject: Config = configLoader

  //
  // Server configurations:
  //

  def controllerRoutesAskTimeout: Duration = configObject.getDuration("bombsquad-server.routes.ask-timeout")

  def serverHost: String = configObject.getString("bombsquad-server.host")

  //
  // MongoDB configurations:
  //

  def mongoDatabase: String = configObject.getString("mongodb.database")

  def mongoURI: String = configObject.getString("mongodb.uri")

  //
  // Game specific configurations:
  //

  def gameDefaultRows: Int = configObject.getInt("bombsquad.default-rows")

  def gameDefaultCols: Int = configObject.getInt("bombsquad.default-cols")

  def gameDefaultBombs: Int = configObject.getInt("bombsquad.default-bombs")
}
