package org.bambucha.watson

import akka.actor.{ActorRef, Actor}
import org.bambucha.watson.messages.{PongMessage, PingMessage}

/**
 * Created by bambucha on 31.08.15.
 */
class IRCPing extends IRCPlugin {
  override def behavior(connection: ActorRef): Receive = {
    case PingMessage(_, server, destination) =>
      connection ! PongMessage(None, server, destination = destination)
  }
}
