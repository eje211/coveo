package com.regularoddity.coveo

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer


/**
  * The definitions for the routes that relate to the locations API.
  */
object CityRoute extends CityRoutes {

  /**
    * The actor system for this route.
    */
  implicit val system: ActorSystem = ActorSystem("coveoRestDemo")
  /**
    * The materializer for this route.
    */
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  /**
    * The registry actor of this route.
    */
  val cityRegistryActor: ActorRef = system.actorOf(CityRegistryActor.props, "cityRegistryActor")

  /**
    * The actual `Route` object, as defined in the underlying trait.
    */
  lazy val routes: Route = cityRoutes
}


/**
  * The entry point for this app.
  */
object QuickstartServer extends App {
  import akka.http.scaladsl.server.Directives._

  /**
    * The main actor system for this app.
    */
  implicit val system: ActorSystem = ActorSystem("coveoRestDemo")
  /**
    * The materializer for this app.
    */
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  /**
    * The concatenation of all the different routes defined in this app.
    */
  lazy val routes: Route = concat(CityRoute.routes)

  Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)

}
