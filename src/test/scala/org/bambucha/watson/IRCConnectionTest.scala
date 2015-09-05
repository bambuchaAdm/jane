package org.bambucha.watson

import akka.actor.Props
import akka.io.Tcp.{Write, Register, Connected}
import akka.util.ByteString
import java.net.InetSocketAddress
import org.bambucha.watson.connection.IRCConnection
import org.bambucha.watson.messages.QuitCommand

/**
 * Created by bambucha on 24.05.14.
 */
class IRCConnectionTest extends ActorTest {

  behavior of classOf[IRCConnection].getSimpleName

  val connectionProps = Props(classOf[IRCConnection], testActor)

  val address = InetSocketAddress.createUnresolved("localhost", 30)

  it should "send simple command via connection" in {
    val ircConnection = system.actorOf(connectionProps)
    ircConnection ! Connected(address, address)
    expectMsgType[Register]
    ircConnection ! QuitCommand()
    expectMsg(Write(ByteString("QUIT\r\n".getBytes)))
  }

  it should "send command with parameters without space" in {
    val ircConnection = system.actorOf(connectionProps)
    ircConnection ! Connected(address, address)
    expectMsgType[Register]
    ircConnection ! QuitCommand("Ala-ma-kota")
    expectMsg(Write(ByteString("QUIT :Ala-ma-kota\r\n".getBytes)))
  }

  it should "send command with parameters with space" in {
    val ircConnection = system.actorOf(connectionProps)
    ircConnection ! Connected(address, address)
    expectMsgType[Register]
    ircConnection ! QuitCommand("Ala ma kota")
    expectMsg(Write(ByteString("QUIT :Ala ma kota\r\n".getBytes)))
  }

}
