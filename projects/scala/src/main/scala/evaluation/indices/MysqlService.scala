package evaluation.indices

import java.util.concurrent.atomic.AtomicInteger

import slick.jdbc.JdbcBackend._
import ws.autocomplete.query.Queries
import ws.indices.MysqlService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object MysqlService extends App {

  // Search texts
  val uris = List("slavery", "music", "biology", "history", "jews", "culture")

  // Function to profile the amount of elapsed time
  def elapsedMs(start: Long): Int = {
    val stop = System.nanoTime()
    ((stop - start) / 1000 / 1000).toInt
  }

  // Create the search-service
  val service = new MysqlService()

  // Latency will decrease progressively
  var currentLatency = 500
  val minimumLatency = 200

  // Objects to compute the statistics
  val count = new AtomicInteger(0)
  val sum = new AtomicInteger(0)

  // Simulate a request
  val futures = (1 to 1000000).toList.map { n =>
    // at "latency" interval
    Thread.sleep(currentLatency)
    currentLatency = math.max(minimumLatency, currentLatency - (currentLatency / 10))

    // generate the request itself
    val start = System.nanoTime()

    service.search(uris.toSet, Some(30), Some(39)).map(rs => {
      val elapsed = elapsedMs(start)

      // Filter a bit the outliers
      if(elapsed > 200) {
        println(s"....ok avg= __, cur=" + elapsed)
      } else {
        // Compute some statistics
        val n = count.incrementAndGet()
        val s = sum.addAndGet(elapsed)
        val avg = (s / n)

        println(s"....ok avg= $avg, cur=" + elapsed)
      }
    }).recover({
      case _ => {
        println(s"failed " + " " + elapsedMs(start))
      }
    })
  }

  Await.result(Future.sequence(futures), Duration.Inf)
}
