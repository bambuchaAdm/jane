package org.bambucha.watson.connection

import akka.actor.{Props, Actor, ActorRef, LoggingFSM}
import org.bambucha.watson.connection.Tokens.{CRLF, Colon, Space}
import org.bambucha.watson.messages._

import scala.util.Success

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

case class PartialPrefix(value: String) extends ParserData {
  def addParameter(param: String): ParserData = throw new RuntimeException("Trying add parameter to prefix")
}

case class ParsedPrefix(value: Option[Prefix]) extends ParserData {
  override def addParameter(param: String): ParserData = throw new RuntimeException("Trying add parameter to prefix")
}

object ParsedPrefix {
  def apply(partialPrefix: PartialPrefix): ParsedPrefix = {
    PrefixParser(partialPrefix.value).Prefix.run() match {
      case Success(prefix) => ParsedPrefix(Option(prefix))
      case _ => ParsedPrefix(None)
    }
  }
}

case class Command(prefix: Option[Prefix], command: String) extends ParserData{
  def addParameter(param: String): ParserData = IRCParsedMessage(prefix, command, List(param))
}

case class IRCParsedMessage(prefix: Option[Prefix], command: String, params: List[String]) extends ParserData {
  def addParameter(param: String): ParserData = copy(params = params :+ param)
  def appendToLastParameter(param: String): ParserData = {
    copy(params = params.updated(params.length-1, params.last + param))
  }
}

object IRCParsedMessage {
  import org.parboiled2.Parser.DeliveryScheme.Throw
  def apply(prefix: Option[String], command: String, params: String*): IRCParsedMessage = {
    val parsedPrefix = prefix.map(rawPrefix => PrefixParser(rawPrefix).Prefix.run())
    apply(parsedPrefix, command, params.toList)
  }
}

class ParserActor(output: ActorRef) extends Actor with LoggingFSM[ParserState, ParserData] {


  startWith(ParseMessage, Empty)

  when(ParseMessage) {
    case Event(Colon, _) => goto(ParsePrefix) using PartialPrefix("")
    case Event(msg: String, Empty) => {
      goto(ParseParameters) using Command(None, msg)
    }
  }

  when(ParsePrefix) {
    case Event(msg: String, PartialPrefix(previews)) => goto(ParsePrefix) using PartialPrefix(msg + previews)
    case Event(Space, state: PartialPrefix) => goto(ParseCommand) using ParsedPrefix(state)
  }

  when(ParseCommand) {
    case Event(msg: String, x: ParsedPrefix) => {
      goto(ParseParameters) using Command(x.value, msg)
    }
  }

  when(ParseParameters) {
    case Event(Space, command: Command) => goto(ParseParameter) using command.addParameter("")
    case Event(CRLF, command: Command) => {
      output ! IRCParsedMessage(command.prefix, command.command, List.empty)
      goto(ParseMessage) using Empty
    }
  }

  when(ParseParameter) {
    case Event(text: String, message: IRCParsedMessage) if message.params.size < 15 => {
      goto(ParseMiddleParameter) using message.appendToLastParameter(text)
    }
    case Event(text: String, message: IRCParsedMessage)=> {
      goto(ParseTrailingParameter) using message.appendToLastParameter(text)
    }
    case Event(Colon, message: IRCParsedMessage) => {
      goto(ParseTrailingParameter) using message
    }
    case Event(CRLF, message: IRCParsedMessage) => {
      sendMessages()
      goto(ParseMessage) using Empty
    }
  }

  when(ParseMiddleParameter) {
    case Event(text: String, message: IRCParsedMessage) => {
      stay using message.appendToLastParameter(text)
    }
    case Event(Colon, message: IRCParsedMessage) => {
      stay using message.appendToLastParameter(":")
    }
    case Event(Space, message: IRCParsedMessage) => {
      goto(ParseParameter) using message.addParameter("")
    }
    case Event(CRLF, message: IRCParsedMessage) => {
      sendMessages()
      goto(ParseMessage) using Empty
    }
  }

  when(ParseTrailingParameter){
    case Event(text: String, message: IRCParsedMessage) => {
      stay using message.appendToLastParameter(text)
    }
    case Event(Colon, message: IRCParsedMessage) => {
      stay using message.appendToLastParameter(":")
    }
    case Event(Space, message: IRCParsedMessage) => {
      stay using message.appendToLastParameter(" ")
    }
    case Event(CRLF, message: IRCParsedMessage) => {
      sendMessages()
      goto(ParseMessage) using Empty
    }
  }

  val motdFolderProps: Props = Props(classOf[MOTDFolder], output)

  def sendMessages(): Unit =  {
    stateData match {
      case message @ IRCParsedMessage(_, NoticeMessage.command, _) =>
        output ! NoticeMessage(message)
      case message @ IRCParsedMessage(_, PingMessage.command, _) =>
        output ! PingMessage(message)
      case message @ IRCParsedMessage(_, PongMessage.command, _) =>
        output ! PongMessage(message)
      case message @ IRCParsedMessage(_, ModeMessage.command, _) =>
        output ! ModeMessage(message)
      case message @ IRCParsedMessage(_, JoinMessage.command, _) =>
        output ! JoinMessage(message)
      case message @ IRCParsedMessage(_, PrivateMessage.command, _) =>
        output ! PrivateMessage(message)

      case msg @ IRCParsedMessage(_, MOTDMessage.beginCommand, _) =>
        context.actorOf(motdFolderProps) ! msg
      case msg @ IRCParsedMessage(_, MOTDMessage.intermediateCommand, _) =>
        context.children.foreach(_ ! msg)
      case msg @ IRCParsedMessage(_, MOTDMessage.endCommand, _) =>
        context.children.foreach(_ ! msg)

      case message: IRCParsedMessage =>
        output ! message
        log.debug(message.toString)
      case _ =>
        log.debug("State data is not IRCPArsedMessage. Didn't send anything")
    }
  }
}
