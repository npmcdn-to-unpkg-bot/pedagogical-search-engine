package ws.indices.bing

import org.json4s.{DefaultFormats, Formats}
import spray.httpx.Json4sSupport

object BingJsonProtocol extends Json4sSupport {
  override implicit def json4sFormats: Formats = DefaultFormats

  case class BingApiResult(d: dElement)
  case class dElement(results: List[ResultElement])
  case class ResultElement(title: String, description: String, url: String)
}
