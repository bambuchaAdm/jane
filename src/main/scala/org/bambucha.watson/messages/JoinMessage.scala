package org.bambucha.watson.messages

import org.bambucha.watson.connection.IRCParsedMessage

case class JoinMessage(prefix: Option[Prefix], channels: List[String]) extends IRCMessage {
  override val command: String = JoinMessage.command
  override val params: List[String] = channels
}

object JoinMessage {
  val command  = "JOIN"

  def apply(prefix: Option[Prefix], channels: String*): JoinMessage = apply(prefix, channels.toList)

  def apply(message: IRCParsedMessage): JoinMessage = {
    val channels = message.params
    JoinMessage(message.prefix, channels)
  }
}
