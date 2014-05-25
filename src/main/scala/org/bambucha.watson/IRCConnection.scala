package org.bambucha.watson

import akka.actor.{ActorRef, ActorLogging, Props, Actor}
import akka.io.{Tcp, IO}
import akka.io.Tcp._
import java.net.InetSocketAddress
import org.bambucha.watson.messages._
import akka.io.Tcp.Connected
import akka.io.Tcp.Register
import akka.io.Tcp.Connect
import akka.io.Tcp.CommandFailed
import akka.util.ByteString
import java.nio.charset.Charset

object IRCConnectionProtocol {
  object Start
  object AuthenticateToServer
}

class IRCConnection(dispatcher: ActorRef, nick: String, username: String, isWallops: Boolean, isInvisible: Boolean, realName: String) extends Actor with ActorLogging {

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

  override def receive: Actor.Receive = {
    case Start =>
      log.debug("Attempt to connecting")
      manager.tell(Connect(new InetSocketAddress("irc.freenode.net",6667)), lexerActor)
    case Connected(remote, local) =>
      connection = sender()
      connection ! Register(lexerActor)
      log.debug("Connected to {} using port {}", remote.getHostName, local.getPort)
    case CommandFailed(command) =>
      log.debug(s"Command failed -> ${command.failureMessage}")
    case command: IRCCommand =>
      val message = command.toMessage
      val commandIdentifier = message.command
      val formatedParameters = formatParameters(message)
      val rawString = Seq(
        commandIdentifier,
        formatedParameters
      ).mkString
      val payload = ByteString(rawString.getBytes(charset))
      connection ! Write(payload)
  }





}
