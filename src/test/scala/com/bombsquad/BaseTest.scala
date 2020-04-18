package com.bombsquad

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.bombsquad.repository.RepositoryStubs
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, Matchers, WordSpec}

trait BaseUnitTest extends AsyncFlatSpec
  with Matchers
  with BeforeAndAfter {

  AppConf.configLoader = ConfigFactory.load("application-test.conf")
}

trait BaseControllerTest extends WordSpec
  with Matchers
  with ScalaFutures
  with ScalatestRouteTest
  with BeforeAndAfter
  with RepositoryStubs {

  AppConf.configLoader = ConfigFactory.load("application-test.conf")
}
