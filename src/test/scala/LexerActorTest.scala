import akka.actor.Props
import akka.util.ByteString

/**
 * Created by bambucha on 11.04.14.
 */
class LexerActorTest extends ActorTest {

  behavior of classOf[LexerActor].getSimpleName

  val lexer = system.actorOf(Props(classOf[LexerActor],testActor))

  import Tokens._

  it should "lex '0x20' as SPACE" in {
    lexer ! ByteString(0x20)
    expectMsg(Space)
  }

  it should "lex '0x38' as COLON" in {
    lexer ! ByteString(0x3b)
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

  it should "lex 0x30 as Digit(0)" in {
    lexer ! ByteString(0x30)
    expectMsg(Digit(0))
  }
}
