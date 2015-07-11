package co.clicktalk.matching

import java.util.concurrent.atomic.AtomicInteger

import co.clicktalk.matching.MatchService.MatchRequest
import com.twitter.finagle.httpx.Response
import com.twitter.util.Promise

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Indexer {
  type Index = mutable.HashMap[String, mutable.Set[Int]]
  type RequestResponse = (MatchRequest, Promise[Response])

  val spoken = new Index()
  val requested = new Index()
  val idMapping = new mutable.HashMap[Int, RequestResponse]
  var dead = new mutable.ListBuffer[Int]
  val nextId = new AtomicInteger(0)

  def findMatch(matchRequest: MatchRequest,
                response: Promise[Response]): Option[RequestResponse] = {

    if (dead.size > 0) {
      Indexer.synchronized {
        dead.foreach(claim)
        dead = new ListBuffer[Int]
      }
    }

    val candidateSpeakers = new mutable.HashSet[Int]
    for {
      lang <- matchRequest.queryAttributes.spokenLanguages
      speakers <- spoken.get(lang)
    } speakers.foreach(candidateSpeakers.add)

    val candidateRequesters = new mutable.HashSet[Int]
    for {
      lang <- matchRequest.userAttributes.spokenLanguages
      requesters <- requested.get(lang)
    } requesters.foreach(candidateRequesters.add)

    val matches = candidateSpeakers.intersect(candidateRequesters).toSeq.sorted
    val matchedId = matches.find { matchId =>
      idMapping.get(matchId) match {
        case Some((_, peerResponse)) => !peerResponse.isInterrupted.isDefined
        case None => false
      }
    }

    matchedId.flatMap(claim)
  }

  def index(matchRequest: MatchRequest, response: Promise[Response]) = {
    val id = nextId.getAndIncrement
    idMapping(id) = (matchRequest, response)

    for (lang <- matchRequest.userAttributes.spokenLanguages) {
      spoken.getOrElseUpdate(lang, new mutable.HashSet[Int]).add(id)
    }

    for (lang <- matchRequest.queryAttributes.spokenLanguages) {
      requested.getOrElseUpdate(lang, new mutable.HashSet[Int]).add(id)
    }

    response.setInterruptHandler {
      case _: Exception =>
        Indexer.synchronized {
          dead.append(id)
        }
    }
  }

  private def claim(id: Int): Option[RequestResponse] = {
    idMapping.get(id) match {
      case Some((peerRequest, peerResponse)) =>
        idMapping.remove(id)

        for (lang <- peerRequest.userAttributes.spokenLanguages) {
          spoken.getOrElseUpdate(lang, new mutable.HashSet[Int]).remove(id)
        }

        for (lang <- peerRequest.queryAttributes.spokenLanguages) {
          requested.getOrElse(lang, new mutable.HashSet[Int]).remove(id)
        }

        Option(peerRequest, peerResponse)

      case _ => None
    }
  }
}
