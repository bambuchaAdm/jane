package org.bambucha.watson

import akka.actor.{ActorLogging, Actor}

class IRCLoggingActor extends Actor with ActorLogging {
  override def receive: Actor.Receive = {
    case message: IRCParsedMessage => log.info(
      s"prefix = '${message.prefix}', command = '${message.command}', parameters = ${message.params}"
    )
  }
}
