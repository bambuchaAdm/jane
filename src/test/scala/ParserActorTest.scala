import akka.actor.Props
import scala.concurrent.duration._

/**
 * Created by bambucha on 15.04.14.
 */
class ParserActorTest extends ActorTest {

  behavior of classOf[ParserActor].getSimpleName

  import Tokens._

  val props = Props(classOf[ParserActor], testActor)

  val prefix = "test"
  val commandCode = "203"
  val planParameter = "hello"
  val middleParameter = "hello:hello"
  val parameterWithSpace = "hello hello"
  val parameterWithLeadingSpace = "  hello hello"

  val numericCommand = Integer.parseInt(commandCode)

  it should "parse message without prefix and parameters" in {
    val parser = system.actorOf(props)
    parser ! commandCode
    parser ! CRLF
    expectMsg(Message(None, numericCommand, List.empty))
  }

  it should "parse message with letter as command" in {
    val parser = system.actorOf(props)
    parser ! "A"
    parser ! CRLF
    expectMsg(Message(None, numericCommand, List.empty))
  }

  it should "parse message without paramters" in {
    val parser = system.actorOf(props)
    parser ! Colon
    parser ! prefix
    parser ! Space
    parser ! commandCode
    parser ! CRLF
    expectMsg(Message(Some(prefix), numericCommand, List.empty))
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
    expectMsg(Message(Some(prefix), numericCommand, List(planParameter)))
  }

  it should "parse message with middle param" in {
    val parser = system.actorOf(props)
    parser ! commandCode
    parser ! Space
    parser ! planParameter
    parser ! Colon
    parser ! planParameter
    parser ! CRLF
    expectMsg(Message(None, numericCommand, List(planParameter + ":" + planParameter)))
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
    expectMsg(Message(None, numericCommand, List(planParameter + " " + planParameter + ":" + planParameter)))
  }

  it should "parse message with 14 parameters with explicit colon on last" in {
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
    val result = receiveOne(100.millisecond).asInstanceOf[Message]
    result.params should have size 15
    result.params.last shouldEqual (planParameter + " " + planParameter)
  }

  it should "parse message with 14 parameters with no colon on last" in {
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
    val result = receiveOne(100.millisecond).asInstanceOf[Message]
    result.params should have size 15
    result.params.last shouldEqual (planParameter + " " + planParameter)
  }
}
