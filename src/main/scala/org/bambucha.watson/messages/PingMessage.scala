package org.bambucha.watson.messages

import org.bambucha.watson.IRCParsedMessage


case class PingMessage(prefix: Option[String] = None, server: String, destination: Option[String] = None) extends IRCMessage {
  override val command: String = PingMessage.command
  override val params: List[String] = List(server) ++ destination
}

object PingMessage {
  val command = "PING"
  def apply(message: IRCParsedMessage): PingMessage = {
    PingMessage(message.prefix, message.params.head, message.params.tail.headOption)
  }
}


