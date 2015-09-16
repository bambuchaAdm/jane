package org.bambucha.watson.messages

import org.bambucha.watson.connection.IRCParsedMessage

case class ModeMessage(prefix: Option[Prefix],
                       subject: String,
                       modesAdded: List[Mode],
                       modesRemoved: List[Mode]) extends IRCMessage {
  override val command: String = ModeMessage.command
  override val params: List[String] = {
    List(subject) ++ modesAdded.map(mode => "+" + mode.symbol) ++ modesRemoved.map(mode => "-" + mode.symbol)
  }
}

object ModeMessage {
  val command = "MODE"

  def apply(message: IRCParsedMessage): ModeMessage = {
    def convertLetterToModes(modes: String): Iterable[Mode] = {
      modes.drop(1).flatMap(UserMode.letterConverter.get)
    }
    val subject = message.params.head
    val modeChanges = message.params.tail
    val addedModes = modeChanges.filter(_.startsWith("+")).flatMap(convertLetterToModes)
    val removedModes = modeChanges.filter(_.startsWith("-")).flatMap(convertLetterToModes)
    ModeMessage(message.prefix, subject, addedModes, removedModes)
  }
}

sealed case class Mode(symbol: Char)

object UserMode {
  val invisible = Mode('i')
  val wallops = Mode('w')
  val restricted = Mode('r')
  val operator = Mode('o')
  val localOperator = Mode('O')

  val all = List(invisible, wallops, restricted, operator, localOperator)

  val letterConverter = all.map(mode => mode.symbol -> mode).toMap
}
