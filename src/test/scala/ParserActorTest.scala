import akka.actor.Props

/**
 * Created by bambucha on 15.04.14.
 */
class ParserActorTest extends ActorTest {

  behavior of classOf[ParserActor].getSimpleName

  import Tokens._
  import ParserActorProtocol._

  val props = Props(classOf[ParserActor], testActor)

  val prefix = "test"
  val commandCode = "203"
  val planParameter = "hello"
  val middleParameter = "hello:hello"
  val parameterWithSpace = "hello hello"
  val parameterWithLeadingSpace = "  hello hello"

  it should "parse message without prefix and parameters" in {
    val parser = system.actorOf(props)
    parser ! commandCode
    parser ! CRLF
    expectMsg(Message(None, Integer.parseInt(commandCode), List.empty))
  }

  it should "parse message without paramters" in {
    val parser = system.actorOf(props)
    parser ! Colon
    parser ! prefix
    parser ! Space
    parser ! commandCode
    parser ! CRLF
    expectMsg(Message(Some(prefix), Integer.parseInt(commandCode), List.empty))
  }

  it should "parse message with paramters" in {
    val parser = system.actorOf(props)
    parser ! Colon
    parser ! prefix
    parser ! Space
    parser ! commandCode
    parser ! Space
    parser ! planParameter
    parser ! CRLF
    expectMsg(Message(Some(prefix), Integer.parseInt(commandCode), List(planParameter)))
  }
}
