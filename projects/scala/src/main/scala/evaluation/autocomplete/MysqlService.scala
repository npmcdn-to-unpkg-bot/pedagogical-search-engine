package evaluation.autocomplete

import java.util.concurrent.atomic.AtomicInteger

import slick.jdbc.JdbcBackend._
import ws.autocomplete.Queries

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object MysqlService extends App {

  val db = Database.forConfig("wikichimp.autocomplete.slick")

  // Search texts
  val one = "abcdefghijklmnopqrstuvwxyz".toList.map(_.toString)
  val twoThre = List(
    "us", "fst", "NY", "LA", "vim", "nlp", "art"):::List(
    "hel", "nat", "sci", "com", "al", "ph", "cog", "wi", "she", "ja"
  )
  val fourPlus = List(
    "natural langage", "finite state", "maths", "logic", "physic",
    "statistics", "particles", "Philosophy", "english", "vine",
    "javascript", "java", "shell", "cell", "business", "music",
    "piano", "guitar", "video games", "computer science", "jazz music",
    "the simpsons", "north america", "string instrument", "São Paulo",
    "los angeles", "us primaries", "french fries", "book"
  ):::List(
    "computer ", "mathematical ", "softw", "paleon", "biolo", "gouvern",
    "natural ", "aspa", "cognit", "conjun", "electrical", "illn",
    "javasc", "finite st", "new yor", "shakesp", "new z"
  ):::List(
    "passe", "super heoros", "litterature", "hotles", "compter sci",
    "java script", "prgram", "naturl ", "elmentary"
  )

  val candidates = one:::twoThre:::fourPlus

  def getText(): String = {
    val i = math.floor(math.random * candidates.length).toInt
    candidates(i)
  }

  // Function to profile the amount of elapsed time
  def elapsedMs(start: Long): Int = {
    val stop = System.nanoTime()
    ((stop - start) / 1000 / 1000).toInt
  }

  // Run the evaluation
  try {
    // Latency will decrease progressively
    var currentLatency = 500;
    val minimumLatency = 10;

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
      val text = getText()

      db.run(Queries.getAction(text, 10).map(rs => {
        val elapsed = elapsedMs(start)

        // Filter a bit the outliers
        if(elapsed > 200) {
          println(s"....ok $text, avg= __, cur=" + elapsed)
        } else {
          // Compute some statistics
          val n = count.incrementAndGet()
          val s = sum.addAndGet(elapsed)
          val avg = (s / n)

          println(s"....ok $text, avg= $avg, cur=" + elapsed)
        }
      })).recover({
        case _ => {
          println(s"failed $text" + " " + elapsedMs(start))
        }
      })
    }

    Await.result(Future.sequence(futures), Duration.Inf)
  } finally db.close
}
