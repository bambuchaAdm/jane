package org.bambucha.watson

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKitBase}
import org.scalatest.{Matchers, BeforeAndAfterAll, FlatSpec}
import org.scalatest.mock.MockitoSugar

/**
 * Created by bambucha on 11.04.14.
 */
abstract class BaseTest extends FlatSpec with Matchers with MockitoSugar

trait TestActorSystem {
  implicit val system = ActorSystem(this.getClass.getSimpleName)
}

abstract class ActorTest extends BaseTest with TestActorSystem with TestKitBase with ImplicitSender with BeforeAndAfterAll {
  override protected def afterAll(): Unit ={
    super.afterAll()
    shutdown(system)
  }
}
