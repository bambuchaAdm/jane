package org.bambucha.watson.plugin

import akka.actor.{Actor, ActorRef}
import org.bambucha.watson.PluginManagerProtocol

abstract class IRCPlugin extends Actor {
  def behavior(connection: ActorRef): Receive

  override def receive: Receive = {
    case PluginManagerProtocol.Connection(handler) =>
      context.become(receive.orElse(behavior(handler)))
  }
}
