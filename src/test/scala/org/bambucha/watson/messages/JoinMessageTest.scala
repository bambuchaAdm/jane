package org.bambucha.watson.messages

import org.bambucha.watson.BaseTest
import org.bambucha.watson.connection.IRCParsedMessage
import org.scalatest.OptionValues

class JoinMessageTest extends BaseTest with OptionValues {
  behavior of "JOIN message"

  private val channel : String = "#botspace-krk"

  it should "have channel list as paramters" in {
    val subjcet = JoinMessage(None, channel, channel)
    subjcet.params should contain theSameElementsAs List(channel, channel)
  }

  it should "transform from IRCParsedMessage" in {
    val subject = JoinMessage(IRCParsedMessage(None, "JOIN", channel ))
    subject.channels should contain only channel
  }
}
