package com.regularoddity.coveo

import akka.actor.{ Actor, ActorLogging, Props }
import akka.util.Timeout
import org.postgresql.geometric.PGpoint

import scala.concurrent.ExecutionContext
final case class Cities(cities: Seq[City])

object CityRegistryActor {
  final case class ActionPerformed(description: String)

  /**
    * Send a message to the [[CityRegistryActor]] to query [[City]] objects from the database based on the given
    * parameters.
    *
    * @param coordinate The locations must be as close as possible to this point.
    * @param location The location's name must be as similar as possible to this name.
    * @param limit The maximum number of results returned we require.
    * @param fuzzy Whether or not the string matching should be fuzzy. Defaults to `true`.
    */
  final case class GetCities(coordinate: PGpoint, location: String, limit: Int = 10, fuzzy: Boolean = true)

  def props: Props = Props[CityRegistryActor]
}

class CityRegistryActor extends Actor with ActorLogging {
  import akka.pattern.pipe
  import scala.concurrent.duration._
  import CityRegistryActor._
  import DatabaseConnection._

  implicit val ec: ExecutionContext = QuickstartServer.system.dispatcher
  implicit val timeout: Timeout = 5.seconds

  def receive: Receive = {
    case GetCities(startLocation: PGpoint, locationName: String, limit: Int, fuzzy: Boolean) =>
      DatabaseConnection.getScore(startLocation, locationName, limit, fuzzy) pipeTo sender()
    //      }
    //    case CreateUser(user) =>
    //      users += user
    //      sender() ! ActionPerformed(s"User ${user.name} created.")
    //    case GetUser(name) =>
    //      sender() ! users.find(_.name == name)
    //    case DeleteUser(name) =>
    //      users.find(_.name == name) foreach { user => users -= user }
    //      sender() ! ActionPerformed(s"User ${name} deleted.")
  }
}