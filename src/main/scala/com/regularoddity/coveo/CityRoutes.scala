package com.regularoddity.coveo

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{ delete, get, post }
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.regularoddity.coveo.CityRegistryActor.GetCities
import org.postgresql.geometric.PGpoint
import PGPoint._

import scala.concurrent.duration._

trait CityRoutes extends JsonSupport {

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[CityRoutes])

  def cityRegistryActor: ActorRef

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val cityRoutes: Route =
    pathPrefix("suggestions") {
      concat(
        pathEnd {
          concat(
            get {
              parameter("startPoint", "locationName", "limit" ? 10, "fuzzy" ? true) {
                (startPointParam, locationName, limit, fuzzy) =>
                  complete(startPointParam.toPointOpt.map((startPoint: PGpoint) => {
                    (cityRegistryActor ? GetCities(startPoint, locationName, limit, fuzzy)).mapTo[Seq[City]]
                  }))
              }
            } //,
          //            post {
          //              entity(as[User]) { user =>
          //                val userCreated: Future[ActionPerformed] =
          //                  (userRegistryActor ? CreateUser(user)).mapTo[ActionPerformed]
          //                onSuccess(userCreated) { performed =>
          //                  log.info("Created user [{}]: {}", user.name, performed.description)
          //                  complete((StatusCodes.Created, performed))
          //                }
          //              }
          //            }
          //          )
          //        },
          //        //#users-get-post
          //        //#users-get-delete
          //        path(Segment) { name =>
          //          concat(
          //            get {
          //              //#retrieve-user-info
          //              val maybeUser: Future[Option[User]] =
          //                (userRegistryActor ? GetUser(name)).mapTo[Option[User]]
          //              rejectEmptyResponse {
          //                complete(maybeUser)
          //              }
          //              //#retrieve-user-info
          //            },
          //            delete {
          //              //#users-delete-logic
          //              val userDeleted: Future[ActionPerformed] =
          //                (userRegistryActor ? DeleteUser(name)).mapTo[ActionPerformed]
          //              onSuccess(userDeleted) { performed =>
          //                log.info("Deleted user [{}]: {}", name, performed.description)
          //                complete((StatusCodes.OK, performed))
          //              }
          //              //#users-delete-logic
          //            }
          )
        }
      )
    }
}
