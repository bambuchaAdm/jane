package org.bambucha.watson

import akka.util.ByteString
import java.nio.charset.Charset

/**
 * Created by bambucha on 11.04.14.
 */

trait Token {
  def value: ByteString
}

object Tokens {

  val defaultCharset = Charset.forName("UTF8")

  case object Space extends Token {
    override val value = ByteString(0x20)
  }

  case object Colon extends Token {
    override val value = ByteString(0x3a)
  }

  case object Null extends Token {
    override val value = ByteString(0x00)
  }

  case object CRLF extends Token {
    override val value = ByteString(0x0D, 0x0A)
  }
}
