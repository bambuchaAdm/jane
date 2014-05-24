package org.bambucha.watson

import akka.actor.{Props, ActorSystem}
import akka.io.{IO, Tcp}
import akka.io.Tcp.Connect
import java.net.InetSocketAddress

object Main extends App {
  implicit val system = ActorSystem.apply("watson")
  val parserProps = Props(classOf[ParserActor], system.deadLetters)
  val parserActor = system.actorOf(parserProps)
  val lexerProps = Props(classOf[LexerActor], parserActor)
  val lexerActor = system.actorOf(lexerProps)
  val manager = IO(Tcp)
  system.log.debug("Attempt to log ")
  manager.tell(Connect(new InetSocketAddress("irc.freenode.net",6667)), lexerActor)
}
