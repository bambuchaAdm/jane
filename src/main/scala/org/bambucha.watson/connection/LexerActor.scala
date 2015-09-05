package org.bambucha.watson.connection

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp._
import akka.util.ByteString
import Tokens.{CRLF, Colon, Null, Space}

class LexerActor(parser: ActorRef) extends Actor with ActorLogging {

  private def processOneOctetTokens(buffer: ByteString): Unit = {
    buffer.take(1) match {
      case Space.value =>
        parser ! Space
        process(buffer.drop(1))
      case Colon.value =>
        parser ! Colon
        process(buffer.drop(1))
      case Null.value =>
        process(buffer.drop(1))
      case _ => processTowOctetTokens(buffer)
    }
  }

  private def processTowOctetTokens(buffer: ByteString): Unit = {
    buffer.take(2) match {
      case CRLF.value =>
        parser ! CRLF
        process(buffer.drop(2))
      case _ => processString(buffer)
    }
  }

  private def processString(buffer: ByteString): Unit = {
    val terminators = Space.value ++ Colon.value ++ CRLF.value
    val terminatorPosition = buffer.indexWhere(byte => terminators.contains(byte))
    if(terminatorPosition != -1){
      parser ! buffer.take(terminatorPosition).utf8String
      process(buffer.drop(terminatorPosition))
    } else {
      parser ! buffer.utf8String
    }
  }

  private def process(buffer: ByteString) : Unit = {
    if(buffer.isEmpty) return
    processOneOctetTokens(buffer)
  }

  var connection: ActorRef = _

  def receive: Actor.Receive = {
    case Received(data) =>
      process(data)
    case msg: ByteString =>
      process(msg)

  }
}
