package org.bambucha.watson

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