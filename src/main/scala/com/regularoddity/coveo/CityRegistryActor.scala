package com.regularoddity.coveo

//#user-registry-actor
import akka.actor.{ Actor, ActorLogging, Props }
import akka.util.Timeout
import org.postgresql.geometric.PGpoint

import scala.concurrent.ExecutionContext
//#user-case-classes
// final case class User(name: String, age: Int, countryOfResidence: String)
final case class Cities(cities: Seq[City])
//#user-case-classes

object CityRegistryActor {
  final case class ActionPerformed(description: String)
  final case class GetCities(startPoint: PGpoint, locationName: String, limit: Int = 10, fuzzy: Boolean = true)
  final case class CreateUser(user: User)
  final case class GetUser(name: String)
  final case class DeleteUser(name: String)

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
      DatabaseConnection.getCities(startLocation, locationName, limit, fuzzy) pipeTo sender()
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
//#user-registry-actor