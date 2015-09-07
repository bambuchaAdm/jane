package org.bambucha.watson.messages

import org.bambucha.watson.BaseTest
import org.parboiled2.ParseError
import org.scalatest.TryValues

class PrefixTest extends BaseTest with TryValues {

  behavior of "Prefix Parser"

  it should "parse server name" in {
    val result = new PrefixParser("wolfe.freenode.net").Prefix.run()
    result.success.value.head shouldEqual ServerName("wolfe.freenode.net")
    println(result.success.value)
  }

  it should "parse user input" in {
    val parser = new PrefixParser("bambucha|wariat!~bambucha@adsl.inetia.pl")
    val result = parser.Prefix.run()
    println(result)
    result.recover{ case any => println(parser.formatError(any.asInstanceOf[ParseError])) }
  }

}
