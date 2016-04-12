package ws.autocomplete

import slick.driver.MySQLDriver.api.actionBasedSQLInterpolation
import slick.jdbc.GetResult
import ws.autocomplete.results.{Disambiguation, PageElement, Result}

object Queries {
  object Codes {
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

  val separator = ",,," // Ensure that no labels or uris contain this
  def preventWildcards(s: String): String =
    s.replaceAll("\\%", "\\\\%").replaceAll("\\_", "\\\\_")

  implicit val getSearchResult: GetResult[Result] = GetResult(r => {
    val source = r.nextInt()

    source match {
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
      case _ => Disambiguation("?", "?", Nil)
    }
  })

  def fourPlus(i: String, n: Int = 10) = {
    val text = preventWildcards(i)
    val textPercent = text + "%"
    sql"""
    (
      SELECT
        #${Codes.Exact.disambiguation} as `Source`,
        MIN(d.`LabelA`) as `LabelA`,
        GROUP_CONCAT(d.`LabelB` SEPARATOR '#$separator') as `LabelB`,
        d.`A` as `UriA`,
        GROUP_CONCAT(d.`B` SEPARATOR '#$separator') as `UriB`,
        GROUP_CONCAT(d.`InB` SEPARATOR '#$separator') as `InB`
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
        GROUP_CONCAT(d.`LabelB` SEPARATOR '#$separator') as `LabelB`,
        d.`A` as `UriA`,
        GROUP_CONCAT(d.`B` SEPARATOR '#$separator') as `UriB`,
        GROUP_CONCAT(d.`InB` SEPARATOR '#$separator') as `InB`
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
    """.as[Result]
  }
}
