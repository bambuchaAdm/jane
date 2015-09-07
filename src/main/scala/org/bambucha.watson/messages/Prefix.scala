package org.bambucha.watson.messages

import org.parboiled2.{CharPredicate, ParserInput, Parser}

case class ServerName(name: String) extends AnyVal

case class Nickname(nick: String) extends AnyVal

case class Username(username: String) extends AnyVal

case class Host(name: String) extends AnyVal

class Prefix {

}

class PrefixParser(val input: ParserInput) extends Parser {

  import CharPredicate._

  def Prefix = rule { (Nick ~ optional( ch('!') ~ User) ~ optional(ch('@') ~ Host)) ~ EOI | Servername ~ EOI }

  def Servername = rule { capture(Hostname) ~> ServerName.apply _ }

  def Nick = rule { capture( (Alpha| Special) ~ zeroOrMore(Alpha | Digit | Special | '-')) ~> Nickname.apply _ }

  def User = rule { capture(oneOrMore(CharPredicate(0x01.toChar to 0x09.toChar) | CharPredicate(0x0B.toChar to 0x0C) | CharPredicate(0x0E.toChar to 0x1F) | CharPredicate(0x21.toChar to 0x3F) | CharPredicate(0x41.toChar to 0xFF))) ~> Username.apply _ }

  def Host = rule { capture(Hostname | Hostaddr) ~> org.bambucha.watson.messages.Host.apply _}

  def Hostaddr = rule { Ip4addr | Ip6addr }

  def Ip6addr = rule { (oneOrMore(HexDigit) ~ 7.times(':' ~ oneOrMore(HexDigit))) | ("0:0:0:0:0:" ~ ( "0" | "FFFF" ) ~ ':' ~ Ip4addr) }

  def Ip4addr = rule { (1 to 3).times(Digit) ~ 3.times('.' | (1 to 3).times(Digit)) }

  def Hostname = rule { Shortname ~ zeroOrMore( ch('.') ~ Shortname ) }

  def Shortname = rule { AlphaNum ~ zeroOrMore(AlphaNum | ch('-')) ~ zeroOrMore(AlphaNum) }

  def Special = rule { anyOf("""[]\‘_^{|}""") }
}
