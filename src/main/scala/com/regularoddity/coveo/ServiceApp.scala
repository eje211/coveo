package com.regularoddity.coveo

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.google.inject.{ Guice, Inject }
import org.apache.logging.log4j.scala.Logging

import scala.util.Try

/**
 * The definitions for the routes that relate to the locations API.
 */
object LocationRoute$ extends LocationRoutes {

  /**
   * The actor system for this route.
   */
  implicit val system: ActorSystem = ActorSystem(ServicesRegistry.conf("main").getString("actor.system"))
  /**
   * The materializer for this route.
   */
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  /**
   * The registry actor of this route.
   */
  val cityRegistryActor: ActorRef = system.actorOf(
    LocationRegistryActor.props,
    ServicesRegistry.conf("main").getString("actor.city")
  )

  /**
   * The actual `Route` object, as defined in the underlying trait.
   */
  lazy val routes: Route = cityRoutes
}

/**
 * The entry point for this app.
 */
object ServiceApp extends App {
  val injector = Guice.createInjector(new ConfigurationModule)
  ServicesRegistry.services.put("locations", injector.getInstance(classOf[Server]))
}

/**
 * @param config How the configuration should be obtained.
 */
class ServerApplication @Inject() (override val config: Configuration) extends Server(config) with Logging {
  import akka.http.scaladsl.server.Directives._

  ServicesRegistry.conf.put("main", config.configuration)

  if (Try(
    "database.postgres_url" ::
      "database.driver" ::
      "actor.city" ::
      "actor.system" :: Nil foreach (conf => config.configuration.getString(conf))
  ).isFailure) {
    logger.error("Bad configuration file. Please check the configuration file.")
    System.exit(1)
  }

  implicit val system = ActorSystem(config.configuration.getString("actor.system"))

  /**
   * The materializer for this app.
   */
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  /**
   * The concatenation of all the different routes defined in this app.
   */
  lazy val routes: Route = concat(LocationRoute$.routes)

  Http().bindAndHandle(routes, "localhost", 8080)

  logger.info(s"Server started on port 8080.")

  Await.result(system.whenTerminated, Duration.Inf)

}

/**
  * A registry of running services and configurations.
  */
object ServicesRegistry {
  /**
    * A registry of running services.
    */
  val services = new ServicesMap()
  /**
    * A registry of running configurations.
    */
  val conf = new ConfMap()
}