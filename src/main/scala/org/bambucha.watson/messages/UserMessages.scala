package org.bambucha.watson.messages

import org.bambucha.watson.IRCMessage

case class PasswordCommand(password: String) extends IRCCommand {
  override def toMessage: IRCMessage = IRCMessage(None, "PASS", List(password))
}

case class NickCommand(nick: String) extends IRCCommand {
  override def toMessage: IRCMessage = IRCMessage(None, "NICK", List(nick))
}

case class UserCommand(username: String, isWallops: Boolean, isInvisible: Boolean, trueName: String) extends IRCCommand {
  override def toMessage: IRCMessage = {
    val wallopsMask = if(isWallops){ 1 << 2 } else { 0 }
    val invisibleMask = if(isInvisible){ 1 << 3} else { 0 }
    val modeMask = (wallopsMask | invisibleMask).toString
    IRCMessage(None, "USER", List(username, modeMask, "*", trueName))
  }
}

case class QuitCommand(message: Option[String]) extends IRCCommand {
  override def toMessage: IRCMessage = {
    IRCMessage(None, "QUIT", message.toList)
  }
}

object QuitCommand {
  def apply(message: String): QuitCommand = QuitCommand(Some(message))

  def apply(): QuitCommand = QuitCommand(None)
}