package org.bambucha.watson

import akka.actor.{ActorRef, Actor}

abstract class IRCPlugin extends Actor {
  def behavior(connection: ActorRef): Receive

  override def receive: Receive = {
    case PluginManagerProtocol.Connection(handler) =>
      context.become(receive.orElse(behavior(handler)))
  }
}
