package org.bambucha.watson

import akka.actor.{Props, ActorLogging, ActorRef}
import org.bambucha.watson.PluginManagerProtocol.RegisterPlugin
import org.bambucha.watson.messages._

class IRCAuth extends IRCPlugin with ActorLogging {
  override def behavior(connection: ActorRef): Receive = {
    case NoticeMessage(_, _, "*** Found your hostname") =>
      connection ! PasswordCommand("alaKota")
      connection ! UserCommand("buczbot", false, true, "Bambuchas bot")
      connection ! NickCommand("buczbot")
      context.stop(self)
      context.parent ! RegisterPlugin(Props[IRCPing],"ping")
    case msg => log.debug("Message unhandled {}", msg)
  }
}
