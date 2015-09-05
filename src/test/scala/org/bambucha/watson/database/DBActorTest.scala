package org.bambucha.watson.database

import akka.actor.Props
import org.bambucha.watson.ActorTest
import org.bambucha.watson.database.DBActorProtocol.{Status, StatusRequest}

class DBActorTest extends ActorTest {

  behavior of "Database actor"

  it should "spawn actor without errors" in {
    val actor = system.actorOf(Props[DBActor])
    actor ! StatusRequest
    val status = expectMsgClass(classOf[Status])
    status.address shouldNot have length 0
  }

}
