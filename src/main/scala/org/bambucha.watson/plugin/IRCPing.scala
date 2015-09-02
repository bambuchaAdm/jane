package org.bambucha.watson.plugin

import akka.actor.ActorRef
import org.bambucha.watson.messages.{PingMessage, PongMessage}

class IRCPing extends IRCPlugin {
  override def behavior(connection: ActorRef): Receive = {
    case PingMessage(_, server, destination) =>
      connection ! PongMessage(None, server, destination)
  }
}
