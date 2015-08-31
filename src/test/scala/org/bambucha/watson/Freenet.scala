package org.bambucha.watson

import akka.actor.{ActorSystem, Props}
import org.bambucha.watson.BotProtocol.Start
import sun.misc.{SignalHandler, Signal}

/**
 * Created by bambucha on 01.06.14.
 */
object Freenet extends App {
  implicit val system = ActorSystem("watson")
  val bot = system.actorOf(Props[Bot])
  bot ! Start
  Signal.handle(new Signal("HUP"), new SignalHandler {
    override def handle(signal: Signal): Unit = {
      system.shutdown()
    }
  })
}
