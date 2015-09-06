package org.bambucha.watson.plugin

import akka.actor.ActorRef
import org.bambucha.watson.messages.JoinMessage

object ChannelManagerProtocol {
  case class JoinChannel(channel: String)
}

class ChannelManager extends IRCPlugin {
  import ChannelManagerProtocol._

  self ! JoinChannel("#botspace-krk")

  override def behavior(connection: ActorRef): Receive = {
    case JoinChannel(channel) =>
      connection ! JoinMessage(None, channel)
  }
}
