import akka.actor.{ActorLogging, ActorRef, Actor}
import akka.util.ByteString

/**
 * Created by bambucha on 11.04.14.
 */
class LexerActor(parser: ActorRef) extends Actor with ActorLogging {

  import Tokens._

  val processOneOctetTokens: (ByteString) => ByteString = { buffer =>
    buffer.take(1) match {
      case Space.value =>
        parser ! Space
        buffer.drop(1)
      case Colon.value =>
        parser ! Colon
        buffer.drop(1)
      case Null.value =>
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

  val processString: (ByteString) => ByteString = { buffer =>
    val terminators = Space.value ++ Colon.value ++ CRLF.value
    buffer.length match {
      case 0 => buffer
      case _ =>
        val terminatorPosition = buffer.indexWhere(byte => terminators.contains(byte))
        if(terminatorPosition != -1){
          parser ! buffer.take(terminatorPosition).utf8String
          buffer.drop(terminatorPosition)
        } else {
          parser ! buffer.utf8String
          ByteString.empty
        }

    }


  }

  val allProcessors = processOneOctetTokens andThen processTowOctetTokens andThen processString

  def process(buffer: ByteString) : Unit = {
    if(buffer.isEmpty) return
    val processedValue = allProcessors(buffer)
    if(processedValue == buffer){
      log.debug(s"value $buffer is cannot be processed")
      return
    }
    process(processedValue)
  }

  def receive: Actor.Receive = {
    case msg: ByteString => process(msg)
  }
}
