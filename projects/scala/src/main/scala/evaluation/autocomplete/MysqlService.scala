package evaluation.autocomplete

import java.util.concurrent.atomic.AtomicInteger

import slick.jdbc.JdbcBackend._
import ws.autocomplete.Queries

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object MysqlService extends App {

  val db = Database.forConfig("wikichimp.autocomplete.slick")

  // Functions to generate the text of each search
  val alphabet = "abcdefghijklmnopqrstuvwxyz"
  def gen(n: Int, pre: String = ""): String = n match {
    case neg if neg <= 0 => pre
    case _ => {
      val i = math.floor(math.random * alphabet.length).toInt
      gen(n - 1, pre + alphabet(i))
    }
  }
  val words = List("spring", "springfield", "simpsons", "coca", "Fanta")
  val pre = List("abu", "spr", "cle", "com", "bio", "car", "fre")
  def gen2(): String = {
    if(math.random < 0.8) {
      val i = math.floor(math.random * words.length).toInt
      words(i)
    } else {
      val i = math.floor(math.random * pre.length).toInt
      pre(i) + gen(1)
    }
  }

  // Function to profile the amount of elapsed time
  def elapsedMs(start: Long): Int = {
    val stop = System.nanoTime()
    ((stop - start) / 1000 / 1000).toInt
  }

  // Run the evaluation
  try {
    var latency = 500;
    val count = new AtomicInteger(0)
    val sum = new AtomicInteger(0)
    val ns = (1 to 10000).toList
    val futures = ns.map { n =>
      Thread.sleep(latency)
      latency = math.max(10, latency - (latency / 10))
      val start = System.nanoTime()
      val text = gen2()
      val test1 = Queries.fourPlus(text, 10)

      db.run(test1.map(rs => {
        val elapsed = elapsedMs(start)
        // Filter the outliers
        if(elapsed > 200) {
          println(s"....ok $text, avg= __, cur=" + elapsed)
        } else {
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
