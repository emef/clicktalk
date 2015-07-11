package co.clicktalk.matching

import com.twitter.finagle.Httpx
import com.twitter.finagle.httpx.Method
import com.twitter.finagle.httpx.path.{->, /, Root}
import com.twitter.finagle.httpx.service.RoutingService
import com.twitter.util.Await

object Server extends App {
  val router = RoutingService.byMethodAndPathObject {
    case Method.Post -> Root / "match" => MatchService
  }

  ServerConfig.parser.parse(args, ServerConfig()) match {
    case Some(conf) =>
      val server = Httpx.serve(s":${conf.port}", router)
      Await.ready(server)

    case None =>
      sys.exit(1)
  }
}
