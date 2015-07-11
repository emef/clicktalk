package co.clicktalk.matching

import scopt.OptionParser

case class ServerConfig(port: Int = 8081)

object ServerConfig {
  val parser = new OptionParser[ServerConfig]("scopt") {
    head("clicktalk matchmaking server")
    opt[Int]('p', "port") action { (x, c) =>
      c.copy(port=x)
    } text "port to run server on"
  }
}
