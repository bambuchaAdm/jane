package org.bambucha.watson.connection

import java.net.InetSocketAddress
import java.nio.charset.Charset

import akka.actor._
import akka.io.Tcp.{CommandFailed, Connect, Connected, Register, _}
import akka.io.{IO, Tcp}
import akka.util.ByteString

object IRCConnectionProtocol {
  object Start
  object AuthenticateToServer
}

class IRCConnection(dispatcher: ActorRef) extends Actor with ActorLogging with Stash {
  import org.bambucha.watson.messages.IRCMessage
  import IRCConnectionProtocol._

  val parserProps = Props(classOf[ParserActor], dispatcher)
  val parserActor = context.actorOf(parserProps, "parser")

  val lexerProps = Props(classOf[LexerActor], parserActor)
  val lexerActor = context.actorOf(lexerProps, "lexer")

  val manager = IO(Tcp)(context.system)

  val charset = Charset.forName("utf-8")

  // FIXME tests
  def formatParameters(message: IRCMessage): String = {
    val parameters = message.params
    val headPayload = parameters.dropRight(1).mkString(" ", " ", "")
    parameters.lastOption.fold("\r\n"){ tailPayload =>
      if(parameters.size < 15){
        s"$headPayload:$tailPayload\r\n"
      } else {
        s"$headPayload$tailPayload\r\n"
      }
    }
  }

  def sendMessages(connection: ActorRef): Actor.Receive = {
    case CommandFailed(command) =>
      log.debug(s"Command failed -> ${command.failureMessage}")
    case command: IRCMessage =>
      val message = command
      log.debug(s"Message to send {}", message)
      val rawString = Seq(
        message.command,
        formatParameters(message)
      ).fold("")(_ + _)
      val payload = ByteString(rawString.getBytes(charset))
      log.debug(s"Try to send '$payload'")
      connection ! Write(payload)
  }

  override def receive: Actor.Receive = {
    case Start =>
      log.debug("Attempt to connecting")
      manager ! Connect(new InetSocketAddress("irc.freenode.net",6667))
    case Connected(remote, local) =>
      unstashAll()
      val connection = sender()
      connection ! Register(lexerActor)
      context.become(sendMessages(connection))
      log.debug("Connected to {} using port {}", remote.getHostName, local.getPort)
    case _ => stash()
  }
}
