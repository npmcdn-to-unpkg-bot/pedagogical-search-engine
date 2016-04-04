package spotlight

import java.util.concurrent.TimeoutException

import dispatch._
import rsc.Types._

import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration._

class WebService(wsHost: String, wsPort: Int, ec: ExecutionContext) {

  val lazyWs = new LazyWebService(wsHost, wsPort, ec)

  def annotateTogether(texts: List[String]): Option[List[Spots]] = {
    val future = lazyWs.annotateTogether(texts)
    // Estimate waiting time
    val time = (30 * texts.size) seconds

    try {
      val spots: List[Spots] = Await.result(future, time)
      Some(spots)
    } catch {
      case e: TimeoutException => None
    }
  }

  def annotateSeparately(texts: List[String]): Option[List[Spots]] = {
    val future = lazyWs.annotateSeparately(texts)
    // Estimate waiting time
    val time = (30 * texts.size) seconds

    try {
      val spots: List[Spots] = Await.result(future, time)
      Some(spots)
    } catch {
      case e: TimeoutException => None
    }
  }

  def annotateSingle(text: String):
  Option[Spots] = annotateSeparately(List(text)).map(_.head)

  def shutdown = lazyWs.shutdown
}
