package org.bambucha.watson.messages

import org.bambucha.watson.BaseTest
import org.bambucha.watson.connection.IRCParsedMessage
import org.scalatest.OptionValues

class PingMessageTest extends BaseTest with OptionValues {
  behavior of "PingMessage"

  val exampleURL = "bucza.eu"

  it should "have sender server" in {
    val subject = PingMessage.apply(IRCParsedMessage(None, "PING", List(exampleURL)))
    subject.server shouldEqual exampleURL
    subject.destination shouldBe empty
  }

  it should "have destination server if present" in {
    val subject = PingMessage.apply(IRCParsedMessage(None, "PING", List(exampleURL, exampleURL)))
    subject.server shouldEqual exampleURL
    subject.destination.value shouldEqual exampleURL
  }
}
