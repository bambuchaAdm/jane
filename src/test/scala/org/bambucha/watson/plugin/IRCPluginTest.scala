package org.bambucha.watson.plugin

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import org.bambucha.watson.{ActorTest, PluginManagerProtocol}

import scala.reflect.ClassTag


class EchoPlugin extends IRCPlugin {
  override def behavior(connection: ActorRef): Receive = {
    case x =>
      connection ! x
  }
}

abstract class PluginTest[A <: IRCPlugin] extends ActorTest {
  import PluginManagerProtocol._

  val connection = TestProbe()

  def subject(args: Any*)(implicit tag: ClassTag[A]): ActorRef = {
    val actor = system.actorOf(Props(tag.runtimeClass, args:_*))
    actor ! Connection(connection.testActor)
    actor
  }
}

class IRCPluginTest extends PluginTest[EchoPlugin] {

  behavior of "IRC Plugin"

  it should "set connection handler on start" in {
    val actor = subject()
    actor ! 1
    connection.expectMsg(1)
  }

  it should "change actor to recived message" in {
    val probe = TestProbe()
    val actor = subject()
    actor ! 1
    connection.expectMsg(1)
    actor ! PluginManagerProtocol.Connection(probe.ref)
    actor ! 2
    probe.expectMsg(2)
  }
}
