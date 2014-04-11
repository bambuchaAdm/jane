import akka.util.ByteString

/**
 * Created by bambucha on 11.04.14.
 */

trait Token {
  def value: ByteString
}

object Tokens {

  case object Space extends Token {
    override val value = ByteString(0x20)
  }

  case object Colon extends Token {
    override val value = ByteString(0x3b)
  }

  case object Null extends Token {
    override val value = ByteString(0x00)
  }

  case object CRLF extends Token {
    override val value = ByteString(0x0D, 0x0A)
  }

  case class Digit(digit: Int) extends Token {
    override val value = ByteString(0x30 + digit)
  }

  object Digit {
    val digits = Range(0x30,0x39).map(ByteString(_))

    def apply(buffer: ByteString): Digit = {
      val asciiOffset = 0x30
      Digit(buffer(0) - asciiOffset)
    }
  }

}
