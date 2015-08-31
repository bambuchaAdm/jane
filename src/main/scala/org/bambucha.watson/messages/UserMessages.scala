package org.bambucha.watson.messages

case class PasswordCommand(password: String) extends IRCMessage {
  override val prefix: Option[String] = None
  override val command: String = "PASS"
  override val params: List[String] = List(password)
}

case class NickCommand(nick: String) extends IRCMessage {
  override val prefix: Option[String] = None
  override val command: String = "NICK"
  override val params: List[String] = List(nick)
}

case class UserCommand(username: String, isWallops: Boolean, isInvisible: Boolean, trueName: String) extends IRCMessage {
  override val prefix: Option[String] = None
  override val command: String = "USER"
  override val params: List[String] = {
    val wallopsMask = if(isWallops){ 1 << 2 } else { 0 }
    val invisibleMask = if(isInvisible){ 1 << 3} else { 0 }
    val modeMask = (wallopsMask | invisibleMask).toString
    List(username, modeMask, "*", trueName)
  }
}

case class QuitCommand(message: Option[String]) extends IRCMessage {
  override val prefix: Option[String] = None
  override val command: String = "QUIT"
  override val params: List[String] = message.toList
}

object QuitCommand {
  def apply(message: String): QuitCommand = QuitCommand(Some(message))
  def apply(): QuitCommand = QuitCommand(None)
}