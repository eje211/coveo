package com.regularoddity.coveo

//#user-registry-actor
import akka.actor.{ Actor, ActorLogging, Props }
import akka.util.Timeout
import org.postgresql.geometric.PGpoint

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
//#user-case-classes
// final case class User(name: String, age: Int, countryOfResidence: String)
final case class Cities(cities: Seq[DatabaseConnection.City])
//#user-case-classes

object CityRegistryActor {
  final case class ActionPerformed(description: String)
  final case object GetCities
  final case class CreateUser(user: User)
  final case class GetUser(name: String)
  final case class DeleteUser(name: String)

  def props: Props = Props[CityRegistryActor]
}

class CityRegistryActor extends Actor with ActorLogging {

  // akka.pattern.pipe needs to be imported
  import akka.pattern.pipe
  // implicit ExecutionContext should be in scope
  import scala.concurrent.duration._
  import CityRegistryActor._
  import DatabaseConnection._

  // implicit val ec: ExecutionContext = context.dispatcher
  implicit val ec: ExecutionContext = QuickstartServer.system.dispatcher
  implicit val timeout: Timeout = 5.seconds

  // var cities = Set.empty[City]

  def receive: Receive = {
    case GetCities =>
      import slick.jdbc.PostgresProfile.api._
      val citiesSelection = for (c <- cities) yield c
      val citiesSubgroup = citiesSelection.take(10).result
      val citiesFuture = db.run(citiesSubgroup)

      citiesFuture.map(dbCities => Cities(dbCities map {
        case (id: Long, name: String, stateProvince: String, fullName: String, countryCode: String, location: PGpoint) =>
          City(id, name, stateProvince, fullName, countryCode, location)
      })) pipeTo sender()
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