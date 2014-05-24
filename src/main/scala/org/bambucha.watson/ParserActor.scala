package org.bambucha.watson

import Tokens.{CRLF, Space, Colon}
import akka.actor.{LoggingFSM, ActorRef, Actor}

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

case class Command(prefix: Option[String], command: String) extends ParserData{
  def addParameter(param: String): ParserData = IRCMessage(prefix, command, List(param))
}

case class IRCMessage(prefix: Option[String], command: String, params: List[String]) extends ParserData {
  def addParameter(param: String): ParserData = copy(params = params :+ param)
  def appendToLastParameter(param: String): ParserData = {
    copy(params = params.updated(params.length-1, params.last + param))
  }
}

class ParserActor(output: ActorRef) extends Actor with LoggingFSM[ParserState, ParserData] {


  startWith(ParseMessage, Empty)

  when(ParseMessage) {
    case Event(Colon, _) => goto(ParsePrefix) using Prefix("")
    case Event(msg: String, Empty) => {
      goto(ParseParameters) using Command(None, msg)
    }
  }

  when(ParsePrefix) {
    case Event(msg: String, Prefix(previews)) => goto(ParsePrefix) using Prefix(msg + previews)
    case Event(Space, state) => goto(ParseCommand) using state
  }

  when(ParseCommand) {
    case Event(msg: String, x: Prefix) => {
      goto(ParseParameters) using Command(Some(x.value), msg)
    }
  }

  when(ParseParameters) {
    case Event(Space, command: Command) => goto(ParseParameter) using command.addParameter("")
    case Event(CRLF, command: Command) => {
      output ! IRCMessage(command.prefix, command.command, List.empty)
      goto(ParseMessage) using Empty
    }
  }

  when(ParseParameter) {
    case Event(text: String, message: IRCMessage) if message.params.size < 15 => {
      goto(ParseMiddleParameter) using message.appendToLastParameter(text)
    }
    case Event(text: String, message: IRCMessage)=> {
      goto(ParseTrailingParameter) using message.appendToLastParameter(text)
    }
    case Event(Colon, message: IRCMessage) => {
      goto(ParseTrailingParameter) using message
    }
    case Event(CRLF, message: IRCMessage) => {
      sendMessages()
      goto(ParseMessage) using Empty
    }
  }

  when(ParseMiddleParameter) {
    case Event(text: String, message: IRCMessage) => {
      stay using message.appendToLastParameter(text)
    }
    case Event(Colon, message: IRCMessage) => {
      stay using message.appendToLastParameter(":")
    }
    case Event(Space, message: IRCMessage) => {
      goto(ParseParameter) using message.addParameter("")
    }
    case Event(CRLF, message: IRCMessage) => {
      sendMessages()
      goto(ParseMessage) using Empty
    }
  }

  when(ParseTrailingParameter){
    case Event(text: String, message: IRCMessage) => {
      stay using message.appendToLastParameter(text)
    }
    case Event(Colon, message: IRCMessage) => {
      stay using message.appendToLastParameter(":")
    }
    case Event(Space, message: IRCMessage) => {
      stay using message.appendToLastParameter(" ")
    }
    case Event(CRLF, message: IRCMessage) => {
      sendMessages()
      goto(ParseMessage) using Empty
    }
  }

  def sendMessages(): Unit =  {
    stateData match {
      case message: IRCMessage =>
        output ! message
        log.debug(message.toString)
      case _ =>
    }
  }
}
