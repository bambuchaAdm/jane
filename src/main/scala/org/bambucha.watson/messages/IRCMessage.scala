package org.bambucha.watson.messages

/**
 * Interface for
 */
abstract class IRCMessage {

  val prefix: Option[Prefix]

  val command: String

  val params: List[String]

}

trait IRCMultiMessage extends Any
