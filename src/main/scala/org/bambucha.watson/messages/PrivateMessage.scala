package org.bambucha.watson.messages

import org.bambucha.watson.connection.IRCParsedMessage

case class PrivateMessage(prefix: Option[String], target: String, message: String) extends IRCMessage {
  override val command: String = PrivateMessage.command
  override val params: List[String] = List(target, message)
}

object PrivateMessage {
  val command = "PRIVMSG"

  def apply(message: IRCParsedMessage): PrivateMessage =
    apply(message.prefix, message.params.head, message.params.last)
}


