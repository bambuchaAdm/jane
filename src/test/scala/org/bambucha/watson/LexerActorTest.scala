package org.bambucha.watson

import akka.actor.Props
import akka.util.ByteString
import java.nio.charset.Charset

/**
 * Created by bambucha on 11.04.14.
 */
class LexerActorTest extends ActorTest {

  behavior of classOf[LexerActor].getSimpleName

  val lexer = system.actorOf(Props(classOf[LexerActor],testActor))

  val charset = Charset.forName("UTF8")

  import Tokens._

  it should "lex ' ' as SPACE" in {
    lexer ! ByteString(" ".getBytes(charset))
    expectMsg(Space)
  }

  it should "lex ':' as COLON" in {
    lexer ! ByteString(":".getBytes(charset))
    expectMsg(Colon)
  }

  it should "omit NULL" in {
    lexer ! ByteString(0x00)
    expectNoMsg()
  }

  it should "lex 0x0D0A as CRLF" in {
    lexer ! ByteString(0x0D, 0x0A)
    expectMsg(CRLF)
  }

  it should "lex 'ala' as string" in {
    lexer ! ByteString("ala".getBytes(charset))
    expectMsg("ala")
  }

  it should "lex ':ala.ma.kota 123'"in {
    lexer ! ByteString(":watson.ma.herbate 123\r\n".getBytes(charset))
    expectMsg(Colon)
    expectMsg("watson.ma.herbate")
    expectMsg(Space)
    expectMsg("123")
    expectMsg(CRLF)
  }

  it should "lex ':hobana.freenode.net NOTICE * :*** Looking up your hostname...'" in {
    lexer ! ByteString(":hobana.freenode.net NOTICE * :*** Looking up your hostname...\r\n".getBytes(charset))
    expectMsg(Colon)
    expectMsg("hobana.freenode.net")
    expectMsg(Space)
    expectMsg("NOTICE")
    expectMsg(Space)
    expectMsg("*")
    expectMsg(Space)
    expectMsg(Colon)
    expectMsg("***")
    expectMsg(Space)
    expectMsg("Looking")
    expectMsg(Space)
    expectMsg("up")
    expectMsg(Space)
    expectMsg("your")
    expectMsg(Space)
    expectMsg("hostname...")
    expectMsg(CRLF)
  }
}
