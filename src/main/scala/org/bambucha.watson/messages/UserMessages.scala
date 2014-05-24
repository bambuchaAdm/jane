package org.bambucha.watson.messages

import org.bambucha.watson.IRCMessage

case class PasswordCommand(password: String) extends IRCCommand {
  override def toMessage: IRCMessage = IRCMessage(None, "PASS", List(password))
}

case class NickCommand(nick: String) extends IRCCommand {
  override def toMessage: IRCMessage = IRCMessage(None, "NICK", List(nick))
}

case class UserCommand()