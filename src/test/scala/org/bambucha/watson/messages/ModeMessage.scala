package org.bambucha.watson.messages

import org.bambucha.watson.BaseTest

class ModeMessageTest extends BaseTest {
  behavior of "ModeMessage"

  val channel: String = "#botspace"

  it should "have plus on added mode  " in {
    val subject = ModeMessage(None, channel, List(UserMode.invisible), List.empty)
    subject.params should contain inOrder(channel, "+i")
  }
}
