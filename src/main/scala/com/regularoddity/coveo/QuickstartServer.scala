package com.regularoddity.coveo

//#quick-start-server
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import org.postgresql.geometric.PGpoint

//#main-class

object CityRoute extends CityRoutes {

  implicit val system: ActorSystem = ActorSystem("coveoRestDemo")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val cityRegistryActor: ActorRef = system.actorOf(CityRegistryActor.props, "cityRegistryActor")

  lazy val routes: Route = cityRoutes
}

object UserRoute extends UserRoutes {

  implicit val system: ActorSystem = ActorSystem("coveoRestDemo")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "cityRegistryActor")

  lazy val routes: Route = userRoutes
}

object QuickstartServer extends App {
  import akka.http.scaladsl.server.Directives._

  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("coveoRestDemo")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //#server-bootstrapping

  //#main-class
  // from the UserRoutes trait
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
