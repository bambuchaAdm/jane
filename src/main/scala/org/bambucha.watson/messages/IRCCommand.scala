package org.bambucha.watson.messages

import org.bambucha.watson.IRCMessage

abstract class IRCCommand {
  def toMessage: IRCMessage
}
