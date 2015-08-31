package org.bambucha.watson.messages

/**
 * Created by bambucha on 31.08.15.
 */
case class PongMessage(prefix: Option[String] = None, server: String, destination: Option[String]) extends IRCMessage {
  override val command: String = "PONG"
  override val params: List[String] = List(server) ++ destination
}
