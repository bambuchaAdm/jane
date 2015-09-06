package org.bambucha.watson.messages

import org.bambucha.watson.connection.IRCParsedMessage

case class PongMessage(prefix: Option[String] = None, server: String, destination: Option[String]) extends IRCMessage {
  override val command: String = PongMessage.command
  override val params: List[String] = List(server) ++ destination
}

object PongMessage {
  val command: String = "PONG"

  def apply(message: IRCParsedMessage): PongMessage = {
    PongMessage(message.prefix, message.params.head, Option(message.params.tail.head))
  }
}

