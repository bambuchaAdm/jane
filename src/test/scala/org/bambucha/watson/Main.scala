package org.bambucha.watson

import akka.actor.{Props, ActorSystem}
import akka.io.{IO, Tcp}
import akka.io.Tcp.Connect
import java.net.InetSocketAddress

object Main extends App {
  implicit val system = ActorSystem.apply("watson")



}
