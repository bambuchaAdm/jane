package org.bambucha.watson.messages

abstract class IRCMessage {

 val prefix: Option[String]

 val command: String

 val params: List[String]

}
