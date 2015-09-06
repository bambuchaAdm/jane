package org.bambucha.watson.messages

import akka.actor.{Actor, ActorRef, ReceiveTimeout}
import org.bambucha.watson.connection.IRCParsedMessage

import scala.concurrent.duration._

case class MOTDMessage(message: String) extends IRCMultiMessage

object MOTDMessage {
  val beginCommand = "375"
  val intermediateCommand = "372"
  val endCommand = "376"
}

class MOTDFolder(output: ActorRef) extends Actor {

  context.setReceiveTimeout(5.seconds)

  val buffer = new StringBuffer()

  override def receive: Receive = {
    case IRCParsedMessage(_, MOTDMessage.beginCommand, text) =>
      buffer.append(text.last.stripMargin('-'))
    case IRCParsedMessage(_, MOTDMessage.intermediateCommand, text) =>
      buffer.append(text.last.stripMargin('-'))
    case IRCParsedMessage(_, MOTDMessage.endCommand, text) =>
      output ! MOTDMessage(buffer.toString)
      context.stop(self)
    case ReceiveTimeout =>
      context.stop(self)
  }
}
