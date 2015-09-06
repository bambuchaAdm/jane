package org.bambucha.watson.database

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor._
import akka.routing.{ActorRefRoutee, Router, RoundRobinRoutingLogic}
import com.mongodb.casbah.Imports._
import org.bambucha.watson.connection.IRCParsedMessage

object DBActorProtocol {
  case object StatusRequest
  case class Status(address: List[String])
}

class DBActor extends Actor with ActorLogging {

  import com.mongodb.casbah.Imports._
  import SupervisorStrategy._
  import DBActorProtocol._

  val config = context.system.settings.config

  val dbHost = config.getString("watson.database.host")

  val dbUser = config.getString("watson.database.user")

  val dbPass = config.getString("watson.database.password")

  val dbName = config.getString("watson.database.name")

  val workersCount = config.getInt("watson.database.workers")

  val driver = MongoClient(dbHost)

  var router = {
    val actors = 1.to(4).map{ id =>
      val actor = context.actorOf(Props(classOf[DBWorker], driver, dbName), id.toString)
      context watch actor
      actor
    }
    Router(RoundRobinRoutingLogic(), actors.map(ActorRefRoutee))
  }

  log.info("Connection to database {} established", driver.address.toString)

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(){
    case _ => Restart
  }

  override def receive: Actor.Receive = {
    case Terminated(ref) =>
      router = router.removeRoutee(ref)
    case StatusRequest =>
      sender() ! Status(driver.allAddress.map(_.toString).toList)
    case msg =>
      log.debug("To save {}", msg.toString)

      router.route((msg, LocalDateTime.now()), sender())
  }
}

class DBWorker(driver: MongoClient, dbName: String) extends Actor with ActorLogging {
  implicit val parsedMessageDatabaseView: (((IRCParsedMessage, LocalDateTime)) => DBObject) = (tp) => {
    val (msg, time) = tp
    MongoDBObject("prefix" -> msg.prefix.getOrElse(""), "command" -> msg.command, "parameters" -> msg.params, "time" -> time.format(DateTimeFormatter.ISO_DATE_TIME))
  }

  val db = driver(dbName)

  val unknown = db("unknown")

  log.info("Connected to database on worker {}", self.path.name)

  override def receive: Receive = {
    case msg: (IRCParsedMessage, LocalDateTime) =>
      unknown.insert(msg)
  }

}
