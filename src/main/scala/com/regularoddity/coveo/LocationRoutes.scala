package com.regularoddity.coveo

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{ delete, get, post }
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.regularoddity.coveo.LocationRegistryActor.GetCities
import org.postgresql.geometric.PGpoint
import PGPoint._
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.duration._

/**
 * REST routes for the location service.
 */
trait LocationRoutes extends JsonSupport with Logging {

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[LocationRoutes])

  def cityRegistryActor: ActorRef

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  /**
   * REST route for the location service.
   */
  lazy val cityRoutes: Route =
    pathPrefix("suggestions") {
      concat(
        pathEnd {
          concat(
            get {
              parameter("startPoint", "locationName", "limit" ? 10, "fuzzy" ? true) {
                (startPointParam, locationName, limit, fuzzy) =>
                  logger.trace(s"GET request received: $startPointParam, $locationName, $limit, $fuzzy.")
                  complete(startPointParam.toPointOpt.map((startPoint: PGpoint) => {
                    (cityRegistryActor ? GetCities(startPoint, locationName, limit, fuzzy)).mapTo[Seq[City]]
                  }))
              }
            }
          )
        }
      )
    }
}
