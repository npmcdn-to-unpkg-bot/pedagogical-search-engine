package ws.autocomplete.query

import slick.driver.MySQLDriver.api.actionBasedSQLInterpolation
import slick.jdbc.GetResult
import ws.autocomplete.results._

import scala.concurrent.ExecutionContext.Implicits.global

object Queries {
  object Codes {
    val ignore = 0
    object Exact {
      val disambiguation = 1
      val title = 2
      val redirect = 3
    }
    object Prefix {
      val disambiguation = 4
      val title = 5
      val redirect = 6
    }
  }
  val defaultLimit = 10

  val separator = ",¬" // Ensure that no labels or uris contain this
  def preventWildcards(s: String): String =
    s.replaceAll("\\%", "\\\\%").replaceAll("\\_", "\\\\_")

  /* mysql GROUP_CONCAT can be truncated, we can send a funky
   * row filled with garbage to set the non-truncated window
   * size. (dirty hack)
   * View also this candidate "solution" http://stackoverflow.com/a/23608554/3890306
   * Benchmark: 4-letters
   *   [.., 512] freq: 10ms - avg-resp: 55ms
   *   [512, 8k] freq: 10?-20?ms - avg-resp: 55ms
   */
  val strPadding = "\"" + "a" * (2^12) + "\""

  // Create the query-action
  def getAction(text: String, n: Int) = text.size match {
    case one if one == 1 => Queries.one(text, n)
    case twoThre if (twoThre == 2 || twoThre == 3) => Queries.twoThre(text, n)
    case _ => Queries.fourPlus(text, n)
  }

  implicit val getSearchResult: GetResult[Result] = GetResult(r => {
    val source = r.nextInt()

    source match {
      case Codes.ignore => Ignore()
      case Codes.Exact.disambiguation | Codes.Prefix.disambiguation => {
        val labelA = r.nextString()
        val labelB = r.nextString().split(separator).toList
        val uriA = r.nextString()
        val uriB = r.nextString().split(separator).toList
        val inB = r.nextString().split(separator).toList.map(_.toInt)

        val bs = (labelB, uriB, inB).zipped.map {
          case (label, uri, in) => PageElement(uri, label, in)
        }
        Disambiguation(uriA, labelA, bs)
      }
      case Codes.Exact.title | Codes.Prefix.title => {
        r.skip
        val label = r.nextString()
        r.skip
        val uri =  r.nextString()
        val in = r.nextInt()
        Title(label, uri, in)
      }
      case Codes.Exact.redirect | Codes.Prefix.redirect => {
        val labelA = r.nextString()
        val labelB = r.nextString()
        r.skip
        val uriB = r.nextString()
        val inB = r.nextInt()
        Redirect(labelA, labelB, uriB, inB)
      }
    }
  })

  def one(i: String, n: Int = defaultLimit) = {
    val text = preventWildcards(i)
    sql"""
    (
      SELECT
        #${Codes.ignore} as `Source`,
        #$strPadding as `LabelA`,
        #$strPadding as `LabelB`,
        #$strPadding as `UriA`,
        #$strPadding as `UriB`,
        1 as `InB`
    ) UNION (
      SELECT
        #${Codes.Exact.disambiguation} as `Source`,
        MIN(d.`LabelA`) as `LabelA`,
        GROUP_CONCAT(d.`LabelB` ORDER BY d.`InB` DESC SEPARATOR '#$separator') as `LabelB`,
        d.`A` as `UriA`,
        GROUP_CONCAT(d.`B` ORDER BY d.`InB` DESC SEPARATOR '#$separator') as `UriB`,
        GROUP_CONCAT(d.`InB` ORDER BY d.`InB` DESC SEPARATOR '#$separator') as `InB`
      FROM `dictionary-disambiguation` d
      WHERE
        d.`LabelA` LIKE $text
      GROUP BY d.`A`
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Exact.title} as `Source`,
        NULL as `LabelA`,
        d.`Label` as `LabelB`,
            NULL as `UriA`,
        d.`Uri` as `UriB`,
        d.`In` as `InB`
      FROM `dictionary-titles` d
      WHERE
        d.`Label` LIKE $text
      ORDER BY d.`In` DESC
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Exact.redirect} as `Source`,
        d.`LabelA` as `LabelA`,
        d.`LabelB` as `LabelB`,
        NULL as `UriA`,
        d.`UriB` as `UriB`,
        d.`InB` as `InB`
      FROM `dictionary-redirects` d
      WHERE
        d.`LabelA` LIKE $text
      ORDER BY d.`InB` DESC
      LIMIT #$n
    );
    """.as[Result].map(rs => rs.filter {
      case Ignore() => false
      case _ => true
    })
  }

  def twoThre(i: String, n: Int = defaultLimit) = {
    val text = preventWildcards(i)
    val textPercent = text + "%"
    sql"""
    (
      SELECT
        #${Codes.ignore} as `Source`,
        #$strPadding as `LabelA`,
        #$strPadding as `LabelB`,
        #$strPadding as `UriA`,
        #$strPadding as `UriB`,
        1 as `InB`
    ) UNION (
      SELECT
        #${Codes.Exact.disambiguation} as `Source`,
        MIN(d.`LabelA`) as `LabelA`,
        GROUP_CONCAT(d.`LabelB` ORDER BY d.`InB` DESC SEPARATOR '#$separator') as `LabelB`,
        d.`A` as `UriA`,
        GROUP_CONCAT(d.`B` ORDER BY d.`InB` DESC SEPARATOR '#$separator') as `UriB`,
        GROUP_CONCAT(d.`InB` ORDER BY d.`InB` DESC SEPARATOR '#$separator') as `InB`
      FROM `dictionary-disambiguation` d
      WHERE
        d.`LabelA` LIKE $text
      GROUP BY d.`A`
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Exact.title} as `Source`,
        NULL as `LabelA`,
        d.`Label` as `LabelB`,
        NULL as `UriA`,
        d.`Uri` as `UriB`,
        d.`In` as `InB`
      FROM `dictionary-titles` d
      WHERE
        d.`Label` LIKE $text
      ORDER BY d.`In` DESC
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Exact.redirect} as `Source`,
        d.`LabelA` as `LabelA`,
        d.`LabelB` as `LabelB`,
        NULL as `UriA`,
        d.`UriB` as `UriB`,
        d.`InB` as `InB`
      FROM `dictionary-redirects` d
      WHERE
        d.`LabelA` LIKE $text
      ORDER BY d.`InB` DESC
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Prefix.disambiguation} as `Source`,
        MIN(d.`LabelA`) as `LabelA`,
        GROUP_CONCAT(d.`LabelB` ORDER BY d.`InB` DESC SEPARATOR '#$separator') as `LabelB`,
        d.`A` as `UriA`,
        GROUP_CONCAT(d.`B` ORDER BY d.`InB` DESC SEPARATOR '#$separator') as `UriB`,
        GROUP_CONCAT(d.`InB` ORDER BY d.`InB` DESC SEPARATOR '#$separator') as `InB`
      FROM `dictionary-disambiguation` d
      WHERE
        d.`LabelA` LIKE $textPercent
      GROUP BY d.`A`
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Exact.title} as `Source`,
        NULL as `LabelA`,
        d.`Label` as `LabelB`,
        NULL as `UriA`,
        d.`Uri` as `UriB`,
        d.`In` as `InB`
      FROM `dictionary-titles` d
      WHERE
        d.`Label` LIKE $textPercent
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Exact.redirect} as `Source`,
        d.`LabelA` as `LabelA`,
        d.`LabelB` as `LabelB`,
        NULL as `UriA`,
        d.`UriB` as `UriB`,
        d.`InB` as `InB`
      FROM `dictionary-redirects` d
      WHERE
        d.`LabelA` LIKE $textPercent
      LIMIT #$n
    );
    """.as[Result].map(rs => rs.filter {
      case Ignore() => false
      case _ => true
    })
  }

  def fourPlus(i: String, n: Int = defaultLimit) = {
    val text = preventWildcards(i)
    val textPercent = text + "%"
    sql"""
    (
      SELECT
        #${Codes.ignore} as `Source`,
        #$strPadding as `LabelA`,
        #$strPadding as `LabelB`,
        #$strPadding as `UriA`,
        #$strPadding as `UriB`,
        1 as `InB`
    ) UNION (
      SELECT
        #${Codes.Exact.disambiguation} as `Source`,
        MIN(d.`LabelA`) as `LabelA`,
        GROUP_CONCAT(d.`LabelB` ORDER BY d.`InB` DESC SEPARATOR '#$separator') as `LabelB`,
        d.`A` as `UriA`,
        GROUP_CONCAT(d.`B` ORDER BY d.`InB` DESC SEPARATOR '#$separator') as `UriB`,
        GROUP_CONCAT(d.`InB` ORDER BY d.`InB` DESC SEPARATOR '#$separator') as `InB`
      FROM `dictionary-disambiguation` d
      WHERE
        d.`LabelA` LIKE $text
      GROUP BY d.`A`
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Exact.title} as `Source`,
        NULL as `LabelA`,
        d.`Label` as `LabelB`,
        NULL as `UriA`,
        d.`Uri` as `UriB`,
        d.`In` as `InB`
      FROM `dictionary-titles` d
      WHERE
        d.`Label` LIKE $text
      ORDER BY d.`In` DESC
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Exact.redirect} as `Source`,
        d.`LabelA` as `LabelA`,
        d.`LabelB` as `LabelB`,
        NULL as `UriA`,
        d.`UriB` as `UriB`,
        d.`InB` as `InB`
      FROM `dictionary-redirects` d
      WHERE
        d.`LabelA` LIKE $text
      ORDER BY d.`InB` DESC
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Prefix.disambiguation} as `Source`,
        MIN(d.`LabelA`) as `LabelA`,
        GROUP_CONCAT(d.`LabelB` ORDER BY d.`InB` DESC SEPARATOR '#$separator') as `LabelB`,
        d.`A` as `UriA`,
        GROUP_CONCAT(d.`B` ORDER BY d.`InB` DESC SEPARATOR '#$separator') as `UriB`,
        GROUP_CONCAT(d.`InB` ORDER BY d.`InB` DESC SEPARATOR '#$separator') as `InB`
      FROM `dictionary-disambiguation` d
      WHERE
        d.`LabelA` LIKE $textPercent
      GROUP BY d.`A`
      ORDER BY length(d.`LabelA`) ASC
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Prefix.title} as `Source`,
        NULL as `LabelA`,
        d.`Label` as `LabelB`,
        NULL as `UriA`,
        d.`Uri` as `UriB`,
        d.`In` as `InB`
      FROM `dictionary-titles` d
      WHERE
        d.`Label` LIKE $textPercent
      ORDER BY length(d.`Label`) ASC, d.`In` DESC
      LIMIT #$n
    ) UNION (
      SELECT
        #${Codes.Prefix.redirect} as `Source`,
        d.`LabelA` as `LabelA`,
        d.`LabelB` as `LabelB`,
        NULL as `UriA`,
        d.`UriB` as `UriB`,
        d.`InB` as `InB`
      FROM `dictionary-redirects` d
      WHERE
        d.`LabelA` LIKE $textPercent
      ORDER BY length(d.`LabelA`) ASC, d.`InB` DESC
      LIMIT #$n
    );
    """.as[Result].map(rs => rs.filter {
      case Ignore() => false
      case _ => true
    })
  }
}
