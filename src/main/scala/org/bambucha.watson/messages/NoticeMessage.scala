package org.bambucha.watson.messages

import org.bambucha.watson.connection.IRCParsedMessage

case class NoticeMessage(prefix: Option[String] = None, target: String, message: String) extends IRCMessage {
  override val command: String = NoticeMessage.command
  override val params: List[String] = List(target, message)
}

object NoticeMessage {
  val command = "NOTICE"

  def apply(message: IRCParsedMessage): NoticeMessage = {
    val target = message.params(0)
    val text = message.params(1)
    NoticeMessage(message.prefix, target, text)
  }
}