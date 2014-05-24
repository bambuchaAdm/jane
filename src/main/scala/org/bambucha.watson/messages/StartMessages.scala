package org.bambucha.watson.messages

case class Welcome(text: String)

case class YourHost(text: String)

case class Created(text: String)

case class Info(serverName: String, version: String, userModes: String, chanelModes: String)

case class Bounce(parameters: List[String])
