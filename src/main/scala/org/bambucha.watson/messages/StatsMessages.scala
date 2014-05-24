package org.bambucha.watson.messages

case class NetworkStatistic(userCount: Int, servicesCount: Int)

case class OperatorStatistic(operatorCount: Int)

case class UnknownUsersStatistic(unknownUsersCount: Int)

case class ChannelStatistic(channelCount: Int)