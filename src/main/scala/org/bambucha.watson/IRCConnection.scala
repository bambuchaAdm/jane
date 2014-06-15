package org.bambucha.watson

import akka.actor._
import akka.io.{Tcp, IO}
import akka.io.Tcp._
import java.net.InetSocketAddress
import org.bambucha.watson.messages._
import akka.util.ByteString
import java.nio.charset.Charset
import org.bambucha.watson.messages.NickCommand
import akka.io.Tcp.Connected
import akka.io.Tcp.Register
import akka.io.Tcp.Connect
import org.bambucha.watson.messages.UserCommand
import org.bambucha.watson.messages.PasswordCommand
import akka.io.Tcp.CommandFailed

object IRCConnectionProtocol {
  object Start
  object AuthenticateToServer
}

class IRCConnection(dispatcher: ActorRef, nick: String, username: String, isWallops: Boolean, isInvisible: Boolean, realName: String) extends Actor with ActorLogging with Stash {

  import IRCConnectionProtocol._

  var connection: ActorRef = _

  val parserProps = Props(classOf[ParserActor], dispatcher)
  val parserActor = context.actorOf(parserProps)

  val lexerProps = Props(classOf[LexerActor], parserActor)
  val lexerActor = context.actorOf(lexerProps)

  val manager = IO(Tcp)(context.system)

  val charset = Charset.forName("utf-8")

  def formatParameters(message: IRCMessage): String = {
    val parameters = message.params
    val headPayload = parameters.dropRight(1).mkString(" ")
    parameters.lastOption.fold(""){ tailPayload =>
      if(parameters.size < 15){
        s"$headPayload :$tailPayload"
      } else {
        s"$headPayload $tailPayload"
      }
    }
  }

  def sendMessages: Actor.Receive = {
    case CommandFailed(command) =>
      log.debug(s"Command failed -> ${command.failureMessage}")
    case command: IRCCommand =>
      val message = command.toMessage
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
      context.become(sendMessages)
      unstashAll()
      connection = sender()
      connection ! Register(lexerActor)
      log.debug("Connected to {} using port {}", remote.getHostName, local.getPort)
    case _ => stash()
  }
}
