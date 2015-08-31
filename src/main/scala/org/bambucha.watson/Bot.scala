package org.bambucha.watson

import akka.actor._
import org.bambucha.watson.BotProtocol.Start
import org.bambucha.watson.PluginManagerProtocol.{Connection, RegisterPlugin}
import org.bambucha.watson.messages._

object PluginManagerProtocol {
  case class RegisterPlugin(plugin: Props, name: String)
  case class Connection(handler: ActorRef)
}

class PluginManager extends Actor with ActorLogging with Stash {

  var plugins = List.empty[ActorRef]

  def work(connection: ActorRef): Actor.Receive = {
    case RegisterPlugin(plugin, name) =>
      log.debug("Adding plugin {}", plugin)
      val instance = context.actorOf(plugin, name)
      plugins = plugins :+ instance
      instance ! Connection(connection)
    case msg: IRCMessage =>
      plugins.foreach( _.tell(msg, connection))
    case Connection(handler) =>
      context.become(work(handler))
  }

  override def receive: Actor.Receive = {
    case Connection(handler) =>
      unstashAll()
      context.become(work(handler))
    case _ => stash()
  }
}

object BotProtocol {
  case object Start
}

class Bot extends Actor {

  import org.bambucha.watson.IRCConnectionProtocol.{Start => ConnectionStart}

  val pluginManager = context.actorOf(Props(classOf[PluginManager]), "pluginManager")

  val ircConnection = context.actorOf(Props(classOf[IRCConnection], pluginManager), "ircConnection")

  pluginManager ! Connection(ircConnection)

  override def receive: Actor.Receive = {
    case Start =>
      pluginManager ! RegisterPlugin(Props[IRCAuth], "authPlugin")
      ircConnection ! ConnectionStart
    case msg: RegisterPlugin => pluginManager.forward(msg)
  }
}
