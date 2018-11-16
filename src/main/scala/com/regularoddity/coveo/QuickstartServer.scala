package com.regularoddity.coveo

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.google.inject.{ Guice, Injector }

/**
 * The definitions for the routes that relate to the locations API.
 */
object CityRoute extends CityRoutes {

  /**
   * The actor system for this route.
   */
  implicit val system: ActorSystem = ActorSystem(AppConfiguration().getString("actor.system"))
  /**
   * The materializer for this route.
   */
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  /**
   * The registry actor of this route.
   */
  val cityRegistryActor: ActorRef = system.actorOf(
    CityRegistryActor.props,
    AppConfiguration().getString("actor.city")
  )

  /**
   * The actual `Route` object, as defined in the underlying trait.
   */
  lazy val routes: Route = cityRoutes
}

/**
 * The entry point for this app.
 */
object QuickstartServer extends App {
  val server = new Server() {}
}

/**
 *
 * @param configuration The configuration to use for this instance.
 * @param system The main actor system for this app.
 */
trait Server {
  import akka.http.scaladsl.server.Directives._

  implicit val system = ActorSystem(AppConfiguration().getString("actor.system"))

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
