package org.bambucha.watson.messages

case class JoinMessage(prefix: Option[String], channels: List[String]) extends IRCMessage {
  override val command: String = JoinMessage.command
  override val params: List[String] = channels
}

object JoinMessage {
  val command  = "JOIN"

  def apply(prefix: Option[String], channels: String*): JoinMessage = apply(prefix, channels.toList)
}
