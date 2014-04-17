import akka.actor.{ActorRef, FSM, Actor}



sealed trait ParserState

case object ParseMessage extends ParserState
case object ParsePrefix extends ParserState
case object ParseCommand extends ParserState
case object ParseParameters extends ParserState
case object ParseParameter extends ParserState
case object ParseMiddleParameter extends ParserState
case object ParseTrailingParameter  extends ParserState

sealed trait ParserData {
  def addParameter(param: String): ParserData
}

case object Empty extends ParserData {
  def addParameter(param: String): ParserData = throw new RuntimeException("Trying add parameter to empty")
}

case class Prefix(value: String) extends ParserData {
  def addParameter(param: String): ParserData = throw new RuntimeException("Trying add parameter to prefix")
}

case class Command(prefix: Option[String], command: Int) extends ParserData{
  import ParserActorProtocol._
  def addParameter(param: String): ParserData = Message(prefix, command, List(param))
}

object ParserActorProtocol {
  case class Message(prefix: Option[String], command: Int, params: List[String]) extends ParserData {
    def addParameter(param: String): ParserData = copy(params = params.::(param))
    def appendToLastParameter(param: String): ParserData = {
      if(params.isEmpty){
        copy(params = List(param))
      } else {
        copy(params = List(param))
        copy(params = params.updated(params.length-1, params.lastOption.fold(param){ _ + param  }))
      }
    }
  }
}

class ParserActor(output: ActorRef) extends Actor with FSM[ParserState, ParserData] {

  import ParserActorProtocol._
  import Tokens._

  startWith(ParseMessage, Empty)

  when(ParseMessage) {
    case Event(Colon, _) => goto(ParsePrefix) using Prefix("")
    case Event(msg: String, Empty) => {
      val commandValue = Integer.parseInt(msg)
      goto(ParseParameters) using Command(None,commandValue)
    }
  }

  when(ParsePrefix) {
    case Event(msg: String, Prefix(previews)) => goto(ParsePrefix) using Prefix(msg + previews)
    case Event(Space, state) => goto(ParseCommand) using state
  }

  when(ParseCommand) {
    case Event(msg: String, x: Prefix) => {
      val commandValue = Integer.parseInt(msg)
      goto(ParseParameters) using Command(Some(x.value), commandValue)
    }
  }

  when(ParseParameters) {
    case Event(Space, command: Command) => goto(ParseParameter) using command.addParameter("")
    case Event(CRLF, command: Command) => {
      output ! Message(command.prefix, command.command, List.empty)
      goto(ParseMessage) using Empty
    }
  }

  when(ParseParameter) {
    case Event(text: String, message: Message) => {
      goto(ParseMiddleParameter) using message.appendToLastParameter(text)
    }
    case Event(Colon, message: Message) => {
      goto(ParseTrailingParameter) using message
    }
    case Event(Space, message: Message) => {
      goto(ParseParameter) using message
    }
    case Event(CRLF, message: Message) => {
      output ! message
      goto(ParseMessage) using Empty
    }
  }

  when(ParseMiddleParameter) {
    case Event(text: String, message: Message) => {
      goto(ParseMiddleParameter) using message.appendToLastParameter(text)
    }
    case Event(Colon, message: Message) => {
      goto(ParseMiddleParameter) using message.appendToLastParameter(":")
    }
    case Event(Space, message: Message) => {
      goto(ParseParameter) using message
    }
    case Event(CRLF, message: Message) => {
      output ! message
      goto(ParseMessage) using Empty
    }
  }

  when(ParseTrailingParameter){
    case Event(text: String, message: Message) => {
      goto(ParseTrailingParameter) using message.appendToLastParameter(text)
    }
    case Event(Colon, message: Message) => {
      goto(ParseTrailingParameter) using message.appendToLastParameter(":")
    }
    case Event(Space, message: Message) => {
      goto(ParseTrailingParameter) using message.appendToLastParameter(" ")
    }
    case Event(CRLF, message: Message) => {
      output ! message
      goto(ParseMessage) using Empty
    }
  }
}
