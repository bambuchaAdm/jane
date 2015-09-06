package org.bambucha.watson.messages

import org.bambucha.watson.BaseTest
import org.bambucha.watson.connection.IRCParsedMessage

class ModeMessageTest extends BaseTest {
  behavior of "ModeMessage"

  val channel: String = "#botspace"

  it should "have plus on added mode  " in {
    val subject = ModeMessage(None, channel, List(UserMode.invisible), List.empty)
    subject.params should contain inOrder(channel, "+i")
  }

  it should "be valiud converted form IRCParsedMessage" in {
    val target: String = "watson"
    val subject = ModeMessage(IRCParsedMessage(None, "MODE", target, "+i", "-o"))
    subject.subject shouldEqual target
    subject.modesAdded should contain only UserMode.invisible
    subject.modesRemoved should contain only UserMode.operator
  }
}
