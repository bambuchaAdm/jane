package org.bambucha.watson.messages

case class ModeMessage(prefix: Option[String],
                       channel: String,
                       modesAdded: List[Mode],
                       modesRemoved: List[Mode]) extends IRCMessage {
  override val command: String = ModeMessage.command
  override val params: List[String] = {
    List(channel) ++ modesAdded.map(mode => "+" + mode.symbol) ++ modesRemoved.map(mode => "-" + mode.symbol)
  }
}

object ModeMessage {
  val command = "MODE"
}

sealed case class Mode(symbol: String)

object Mode {
  val away = Mode("a")
  val invisible = Mode("i")
  val wallops = Mode("w")
  val restricted = Mode("r")
  val operator = Mode("o")
  val localOperator = Mode("O")
  val serverNotice = Mode("s")
}
