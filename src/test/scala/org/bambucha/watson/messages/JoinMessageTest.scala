package org.bambucha.watson.messages

import org.bambucha.watson.BaseTest

class JoinMessageTest extends BaseTest {
  behavior of "JOIN message"

  it should "have channel list as paramters" in {
    val channel: String = "#botspace-krk"
    val subjcet = JoinMessage(None, channel, channel)
    subjcet.params should contain theSameElementsAs List(channel, channel)
  }
}
