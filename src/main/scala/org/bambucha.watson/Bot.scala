package org.bambucha.watson

import akka.actor._
import org.bambucha.watson.BotProtocol.Start
import org.bambucha.watson.PluginManagerProtocol.{Connection, RegisterPlugin}
import org.bambucha.watson.connection.{IRCConnection, IRCConnectionProtocol}
import org.bambucha.watson.plugin.IRCAuth

object BotProtocol {
  case object Start
}

class Bot extends Actor {

  import IRCConnectionProtocol.{Start => ConnectionStart}

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
