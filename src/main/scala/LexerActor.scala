import akka.actor.{ActorLogging, ActorRef, Actor}
import akka.util.ByteString

/**
 * Created by bambucha on 11.04.14.
 */

class LexerException extends Throwable

class LexerActor(parser: ActorRef) extends Actor with ActorLogging {

  import Tokens._

  val processOneOctetTokens: (ByteString) => ByteString = { buffer =>
    println(buffer)
    buffer.take(1) match {
      case Space.value =>
        parser ! Space
        buffer.drop(1)
      case Colon.value =>
        parser ! Colon
        buffer.drop(1)
      case Null.value =>
        buffer.drop(1)
      case msg if Digit.digits.contains(msg) =>
        parser ! Digit(msg)
        buffer.drop(1)
      case _ => buffer
    }
  }

  val processTowOctetTokens: (ByteString) => ByteString = { buffer =>
    buffer.take(2) match {
      case CRLF.value =>
        parser ! CRLF
        buffer.drop(2)
      case _ => buffer
    }
  }

  val allProcessors = processOneOctetTokens andThen processTowOctetTokens

  def process(buffer: ByteString) : Unit = {
    if(buffer.isEmpty) return

    val processedValue = allProcessors(buffer)
    if(processedValue == buffer) throw new LexerException
    process(processedValue)
  }

  def receive: Actor.Receive = {
    case msg: ByteString => process(msg)
  }
}
