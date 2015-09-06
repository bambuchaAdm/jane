package org.bambucha.watson.plugin

import akka.actor.{ActorLogging, ActorRef, Props}
import org.bambucha.watson.PluginManagerProtocol.RegisterPlugin
import org.bambucha.watson.messages._

class IRCAuth extends IRCPlugin with ActorLogging {
  override def behavior(connection: ActorRef): Receive = {
    case NoticeMessage(_, _, "*** Found your hostname") =>
      connection ! PasswordCommand("alaKota")
      connection ! UserCommand("buczbot", false, true, "Bambuchas bot")
      connection ! NickCommand("buczbot")
      context.stop(self)
      context.parent ! RegisterPlugin(Props[IRCPing], "ping")
      context.parent ! RegisterPlugin(Props[ChannelManager],"channelManager")
    case msg => log.debug("Message unhandled {}", msg)
  }
}