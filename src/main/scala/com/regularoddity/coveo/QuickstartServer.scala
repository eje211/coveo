package com.regularoddity.coveo

//#quick-start-server
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

//#main-class

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

object UserRoute extends UserRoutes {

  implicit val system: ActorSystem = ActorSystem("coveoRestDemo")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "cityRegistryActor")

  lazy val routes: Route = userRoutes
}


/**
  * The entry point for this app.
  */
object QuickstartServer extends App {
  import akka.http.scaladsl.server.Directives._

  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  /**
    * The main actor system for this app.
    */
  implicit val system: ActorSystem = ActorSystem("coveoRestDemo")
  /**
    * The materializer for this app.
    */
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //#server-bootstrapping

  //#main-class
  /**
    * The concatenation of all the different routes defined in this app.
    */
  lazy val routes: Route = concat(CityRoute.routes, UserRoute.routes)
  //#main-class

  //#http-server
  Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class

}
//#main-class
//#quick-start-server
