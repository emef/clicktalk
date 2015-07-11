package co.clicktalk.matching

import com.twitter.finagle.Service
import com.twitter.finagle.httpx.{Request, Response, Status}
import com.twitter.util.Future
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods

/**
 * POST /match
 * ------
 *
 * Parses request into a MatchRequest and forwards it to the MatchMaker.
 */
object MatchService extends Service[Request, Response] {
  implicit val formats = DefaultFormats

  case class MatchAttributes(spokenLanguages: List[String])
  case class MatchRequest(userId: Int,
                          userAttributes: MatchAttributes,
                          queryAttributes: MatchAttributes)
  case class MatchedUser(userId: Int,
                         userAttributes: MatchAttributes)
  case class MatchedEnvelope(user: MatchedUser,
                             publishStreamId: String,
                             subscribeStreamId: String)
  case class SuccessResponse(matched: MatchedEnvelope)
  case class ErrorResponse(error: String)

  override def apply(request: Request): Future[Response] = {
    parseMatchRequest(request) match {
      case Some(matchRequest) => MatchMaker.findMatch(matchRequest)
      case None => Future.value(Response(request.version, Status.BadRequest))
    }
  }

  def parseMatchRequest(request: Request): Option[MatchRequest] = {
    try {
      val body = JsonMethods.parse(request.contentString)
      Option.apply(body.extract[MatchRequest])

    } catch {
      case _: Exception => Option.empty
    }
  }
}
