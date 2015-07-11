package co.clicktalk.matching

import java.io.{PrintWriter, StringWriter}
import java.util.concurrent.LinkedBlockingQueue

import co.clicktalk.matching.MatchService.{MatchedUser, MatchedEnvelope, SuccessResponse, MatchRequest}
import com.twitter.finagle.httpx.{Response, Status}
import com.twitter.util.{Future, Promise}
import org.json4s.DefaultFormats
import org.json4s.Extraction.decompose
import org.json4s.native.JsonMethods.{pretty, render}

object MatchMaker {
  implicit val defaultFormats = DefaultFormats
  private val matchQueue = new LinkedBlockingQueue[(MatchRequest, Promise[Response])]()

  def findMatch(matchRequest: MatchRequest): Future[Response] = {
    val promise: Promise[Response] = new Promise[Response]
    matchQueue.put((matchRequest, promise))
    promise
  }

  private val matcherThreadBody: Runnable = new Runnable {
    def run() = {
      while(true) {
        val (matchRequest, promise) = matchQueue.take()
        if (!promise.isInterrupted.isDefined) {
          try {
            handleNextRequest(matchRequest, promise)
          } catch {
            case e: Exception =>
              val response = Response(Status.InternalServerError)
              val stringWriter = new StringWriter
              e.printStackTrace(new PrintWriter(stringWriter))
              response.setContentString(stringWriter.toString)
              promise.setValue(response)
          }
        }
      }
    }
  }

  private def handleNextRequest(matchRequest: MatchRequest, response: Promise[Response]) = {
    Indexer.findMatch(matchRequest, response) match {
      case Some((peerRequest, peerResponse)) =>
        val userStreamId = java.util.UUID.randomUUID.toString
        val peerStreamId = java.util.UUID.randomUUID.toString

        sendMatchResponse(response, peerRequest, userStreamId, peerStreamId)
        sendMatchResponse(peerResponse, matchRequest, peerStreamId, userStreamId)

      case None =>
        Indexer.index(matchRequest, response)
    }
  }

  private def sendMatchResponse(response: Promise[Response],
                                peerMatchRequest: MatchRequest,
                                publishStreamId: String,
                                subscribeStreamId: String) = {
    val matchedUser = MatchedUser(peerMatchRequest.userId, peerMatchRequest.userAttributes)
    val envelope = new MatchedEnvelope(matchedUser, publishStreamId, subscribeStreamId)
    val success = new SuccessResponse(envelope)

    val httpResp = Response(Status.Ok)
    httpResp.setContentTypeJson()
    httpResp.setContentString(pretty(render(decompose(success))))
    response.setValue(httpResp)
  }

  private val matchThread = new Thread(matcherThreadBody)
  matchThread.start()
}
