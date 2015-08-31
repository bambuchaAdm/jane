package org.bambucha.watson

import akka.actor.{Props, ActorRef}
import akka.testkit.TestProbe


class EchoPlugin extends IRCPlugin {
  override def behavior(connection: ActorRef): Receive = {
    case x =>
      connection ! "echo"
  }
}

class IRCPluginTest extends ActorTest {

  behavior of "IRC Plugin"

  it should "set connection handler on start" in {
    val actor = system.actorOf(Props[EchoPlugin])
    actor ! PluginManagerProtocol.Connection(testActor)
    actor ! 1
    expectMsg("echo")
  }

  val probe = TestProbe()

  it should "change actor to recived message" in {
    val actor = system.actorOf(Props[EchoPlugin])
    actor ! PluginManagerProtocol.Connection(testActor)
    actor ! 1
    expectMsg("echo")
    actor ! PluginManagerProtocol.Connection(probe.ref)
    actor ! 1
    probe.expectMsg("echo")
  }
}
