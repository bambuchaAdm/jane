package org.bambucha.watson

import akka.actor.Props
import org.bambucha.watson.connection.{Tokens, IRCParsedMessage, ParserActor}
import org.bambucha.watson.messages.{ServerName, NoticeMessage}
import scala.concurrent.duration._

/**
 * Created by bambucha on 15.04.14.
 */
class ParserActorTest extends ActorTest {

  behavior of classOf[ParserActor].getSimpleName

  import Tokens._

  val props = Props(classOf[ParserActor], testActor)

  val prefix = "irc.freenode.net"
  val commandCode = "203"
  val planParameter = "hello"
  val middleParameter = "hello:hello"
  val parameterWithSpace = "hello hello"
  val parameterWithLeadingSpace = "  hello hello"
  val testPrefix = Some(ServerName("irc.freenode.net"))

  val numericCommand = commandCode

  it should "parse message without prefix and parameters" in {
    val parser = system.actorOf(props)
    parser ! commandCode
    parser ! CRLF
    expectMsg(IRCParsedMessage(None, numericCommand, List.empty))
  }

  it should "parse message without paramters" in {
    val parser = system.actorOf(props)
    parser ! Colon
    parser ! prefix
    parser ! Space
    parser ! commandCode
    parser ! CRLF

    expectMsg(IRCParsedMessage(testPrefix, numericCommand, List.empty))
  }

  it should "parse message with plain paramters" in {
    val parser = system.actorOf(props)
    parser ! Colon
    parser ! prefix
    parser ! Space
    parser ! commandCode
    parser ! Space
    parser ! planParameter
    parser ! CRLF
    expectMsg(IRCParsedMessage(testPrefix, numericCommand, List(planParameter)))
  }

  it should "parse message with middle param" in {
    val parser = system.actorOf(props)
    parser ! commandCode
    parser ! Space
    parser ! planParameter
    parser ! Colon
    parser ! planParameter
    parser ! CRLF
    expectMsg(IRCParsedMessage(None, numericCommand, List(planParameter + ":" + planParameter)))
 }

  it should "parse message with talling param" in {
    val parser = system.actorOf(props)
    parser ! commandCode
    parser ! Space
    parser ! Colon
    parser ! planParameter
    parser ! Space
    parser ! planParameter
    parser ! Colon
    parser ! planParameter
    parser ! CRLF
    expectMsg(IRCParsedMessage(None, numericCommand, List(planParameter + " " + planParameter + ":" + planParameter)))
  }

  it should "parse message with 15 parameters with explicit colon on last" in {
    val parser = system.actorOf(props)
    parser ! commandCode
    Range(0,14).toList.foreach{ _ =>
      parser ! Space
      parser ! planParameter
    }
    parser ! Space
    parser ! Colon
    parser ! planParameter
    parser ! Space
    parser ! planParameter
    parser ! CRLF
    val result = receiveOne(100.millisecond).asInstanceOf[IRCParsedMessage]
    result.params should have size 15
    result.params.last shouldEqual (planParameter + " " + planParameter)
  }

  it should "parse message with 15 parameters with no colon on last" in {
    val parser = system.actorOf(props)
    parser ! commandCode
    Range(0,14).toList.foreach{ _ =>
      parser ! Space
      parser ! planParameter
    }
    parser ! Space
    parser ! planParameter
    parser ! Space
    parser ! planParameter
    parser ! CRLF
    val result = receiveOne(100.millisecond).asInstanceOf[IRCParsedMessage]
    result.params should have size 15
    result.params.last shouldEqual (planParameter + " " + planParameter)
  }

  it should "parse NOTICE message tokens" in {
    val parser = system.actorOf(props)
    parser ! Colon
    parser ! "hobana.freenode.net"
    parser ! Space
    parser ! "NOTICE"
    parser ! Space
    parser ! "*"
    parser ! Space
    parser ! Colon
    parser ! "***"
    parser ! Space
    parser ! "Looking"
    parser ! Space
    parser ! "up"
    parser ! Space
    parser ! "your"
    parser ! Space
    parser ! "hostname..."
    parser ! CRLF
    expectMsg(NoticeMessage(Option(ServerName("hobana.freenode.net")), "*", "*** Looking up your hostname..."))
  }
}
